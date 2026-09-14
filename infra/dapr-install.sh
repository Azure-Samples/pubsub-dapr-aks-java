#!/bin/sh

set -o errexit

echo "📀 - Post-Provision hook - Installing DAPR AKS..." 

# updating kubeconfig with cluster credentials
az aks get-credentials --resource-group $AZURE_RESOURCE_GROUP_NAME --name $AZURE_AKS_CLUSTER_NAME --overwrite-existing

az config set extension.use_dynamic_install=yes_without_prompt

# check if DAPR namespace exists on cluster, and skip installation if it does
if [ $(kubectl get namespaces | grep -c "^dapr-system ") -eq "0" ]; then
    echo "- Installing DAPR on AKS..." 
    az k8s-extension create --cluster-type managedClusters \
        --cluster-name $AZURE_AKS_CLUSTER_NAME \
        --resource-group $AZURE_RESOURCE_GROUP_NAME \
        --name myDaprExtension \
        --extension-type Microsoft.Dapr
else
    echo "\t - DAPR already installed on AKS..."
fi 

# check if namespace used by azd already exists on cluster
if [ $(kubectl get namespaces | grep -c "^$AZURE_ENV_NAME ") -eq "0" ]; then
    echo "- Creating $AZURE_ENV_NAME namespace..." 
    kubectl create namespace $AZURE_ENV_NAME
fi 

printf '\n🚀 Deploy Redis on AKS\n\n'
if ! kubectl get secret redis --namespace "$AZURE_ENV_NAME" >/dev/null 2>&1; then
    redis_password=$(openssl rand -hex 32)
    printf '%s' "$redis_password" | kubectl create secret generic redis --namespace "$AZURE_ENV_NAME" \
        --from-file='redis-password'=/dev/stdin
fi
kubectl apply -f ./infra/redis.yaml --namespace "$AZURE_ENV_NAME" --wait=true
kubectl rollout status statefulset/redis-master --namespace "$AZURE_ENV_NAME" --timeout=300s
kubectl rollout status statefulset/redis-replicas --namespace "$AZURE_ENV_NAME" --timeout=300s

printf '\n🚀 Deploy pub-sub broker component backed by Redis\n\n'
kubectl apply -f ./local/components/pubsub.yaml --wait=true --namespace $AZURE_ENV_NAME

printf '\n🚀 Deploy state store component backed Redis\n\n'
kubectl apply -f ./local/components/state.yaml --wait=true --namespace $AZURE_ENV_NAME