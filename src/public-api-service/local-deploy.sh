#!/bin/sh

set -o errexit

serviceName="public-api-service"
version=$(date +%Y.%m.%d.%H.%M.%S)
printf "\n🛖  Releasing version: %s\n\n" "${version}"

# check if service deployment exists on cluster, deleting if it does
if [ $(kubectl get deployments | grep -c "^${serviceName}") -eq "1" ]; then
  printf "\n☢️  Attempting to delete existing deployment %s\n\n" "${serviceName}"
  kubectl delete deployment "${serviceName}"
fi

printf "\n🏗️  Building docker image\n\n"
docker build -t localhost:5001/"${serviceName}":"${version}" .

printf "\n🚚  Pushing docker image to local registry\n\n"
docker push localhost:5001/"${serviceName}":"${version}"

printf "\n🚀  Deploying to cluster\n\n"
cat <<EOF | kubectl apply -f -

kind: Service
apiVersion: v1
metadata:
  name: ${serviceName}
  labels:
    app: ${serviceName}
spec:
  selector:
    app: ${serviceName}
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8080
  type: LoadBalancer
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ${serviceName}
  labels:
    app: ${serviceName}
spec:
  replicas: 1
  selector:
    matchLabels:
      app: ${serviceName}
  template:
    metadata:
      labels:
        app: ${serviceName}
      annotations:
        dapr.io/enabled: "true"
        dapr.io/app-id: "${serviceName}"
        dapr.io/app-port: "8080"
        dapr.io/enable-api-logging: "true"
    spec:
      containers:
      - name: node
        image: localhost:5001/${serviceName}:${version}
        env:
        - name: APP_PORT
          value: "8080"
        - name: APP_VERSION
          value: "${version}"
        ports:
        - containerPort: 80
        imagePullPolicy: Always
EOF


printf "\n🎉  Deployment complete\n\n"