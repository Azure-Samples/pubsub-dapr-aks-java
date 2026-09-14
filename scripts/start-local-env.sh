#!/bin/sh
set -o errexit

printf "\n🤖 Starting local environment...\n\n"

printf '\n📀 Create registry container unless it already exists\n\n'
reg_name='kind-registry'
reg_port='5001'
if [ "$(docker inspect -f '{{.State.Running}}' "${reg_name}" 2>/dev/null || true)" != 'true' ]; then
  docker run \
    -d --restart=always -p "127.0.0.1:${reg_port}:5000" --name "${reg_name}" \
    registry:2
fi

printf '\n📀 Create kind cluster called: azd-aks\n\n'
kind create cluster --name azd-aks --config ./local/kind-cluster-config.yaml

printf '\n📀 Connect the registry to the cluster network if not already connected\n'
if [ "$(docker inspect -f='{{json .NetworkSettings.Networks.kind}}' "${reg_name}")" = 'null' ]; then
  docker network connect "kind" "${reg_name}"
fi

printf '\n📀 Map the local registry to cluster\n\n'
kubectl apply -f ./local/deployments/config-map.yaml --wait=true


printf '\n📀 Deploy Redis\n\n'
if ! kubectl --context kind-azd-aks get secret redis --namespace default >/dev/null 2>&1; then
  redis_password=$(openssl rand -hex 32)
  printf '%s' "$redis_password" | kubectl --context kind-azd-aks create secret generic redis --namespace default \
    --from-file='redis-password'=/dev/stdin
fi
# Keep local Redis standalone; AKS also deploys the replica resources.
kubectl --context kind-azd-aks apply -f ./infra/redis.yaml --namespace default \
  --selector='app.kubernetes.io/component!=replica' --wait=true
kubectl --context kind-azd-aks rollout status statefulset/redis-master --namespace default --timeout=300s

printf '\n📀 Init Dapr\n\n'
dapr init --kubernetes --wait --timeout 600

printf '\n📀 Deploy pub-sub broker component backed by Redis\n\n'
kubectl apply -f ./local/components/pubsub.yaml --wait=true

printf '\n📀 Deploy state store component backed Redis\n\n'
kubectl apply -f ./local/components/state.yaml --wait=true


printf "\n🎉 Local environment setup completed!\n\n"