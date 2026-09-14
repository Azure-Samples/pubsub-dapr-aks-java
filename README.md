---
page_type: sample
languages:
- azdeveloper
- java
- bicep
products:
- azure
- azure-kubernetes-service
urlFragment: pubsub-dapr-aks-java
name: Java Pub/Sub Sample using Kubernetes and DAPR
description: Demonstrate a pub/sub messaging architecture using Dapr for a Java application running in a Kubernetes cluster. This sample project implements an event-driven communication between different components of the system using the pub/sub pattern. 
---
<!-- YAML front-matter schema: https://review.learn.microsoft.com/en-us/help/contribute/samples/process/onboarding?branch=main#supported-metadata-fields-for-readmemd -->

# Java Pub/Sub Sample using Kubernetes and DAPR

This sample is to demonstrate a pub/sub messaging architecture using Dapr for a Java application running in a Kubernetes cluster. This sample project implements an event-driven communication between different components of the system using the pub/sub pattern. By leveraging Dapr, this sample provides an asynchronous processing in a distributed system and application of side car pattern in a Kubernetes cluster. Whether you are new to event-driven architectures or looking to explore Dapr's capabilities for a Java application running in Kubernetes, this sample is a great place to start.

This sample doesn't implement a great deal of distributed systems concerns like Idempotency, Fault Tolerance, Retries etc. at the application level. Instead, it focuses on the application architecture and how Dapr can be used to implement a pub/sub messaging pattern in a Java application running in Kubernetes.

The sample is built using [Azure Developer CLI](https://learn.microsoft.com/en-us/azure/developer/azure-developer-cli/make-azd-compatible?pivots=azd-create) conventions to run on Azure. Alternatively, you can run the project in your local environment using [Kind](https://kind.sigs.k8s.io/docs/user/quick-start/) and [Docker](https://docs.docker.com/get-docker/).

## How it works?

This sample implements a simple pub/sub workflow:

1. Public API endpoint receives new money transfer request. [TRANSFER(Sender: A, Amount: 100, Receiver:B)]
1. Request is published to pub/sub broker.
1. Transfer workflow starts
    1. Fraud service checks the legitimacy of the operation and triggers [VALIDATED(Sender: A, Amount: 100, Receiver:B)]
    1. Account service checks if `Sender` has enough funds and triggers [APPROVED(Sender: A, Amount: 100, Receiver: B)]
1. Public API can be used to check if there is a confirmation of the money transfer request.

![Workflow](/docs/flow.drawio.png)

## Services

The project contains the following services:

- [Public API](/src/public-api-service) - Public API endpoint that receives new money transfer requests, starts workflow and checks customer notifications.
- [Fraud Service](/src/fraud-service) - Fraud service that checks the legitimacy of the operation.
- [Account Service](/src/account-service) - Account service that checks if `Sender` has enough funds.

## Prerequisites

Following technologies and CLIs are used for the development. Follow the links to install them:

- [Azure Developer CLI](https://learn.microsoft.com/en-us/azure/developer/azure-developer-cli/make-azd-compatible?pivots=azd-create)
- [Dapr CLI](https://docs.dapr.io/getting-started/install-dapr-cli/)
- [Docker](https://docs.docker.com/get-docker/)
- [Kubernetes](https://kubernetes.io/docs/tasks/tools/)
- [Helm](https://helm.sh/docs/intro/install/)
- [Redis](https://learn.microsoft.com/en-us/azure/azure-cache-for-redis/)
- [Kind](https://kind.sigs.k8s.io/docs/user/quick-start/)
- [Spring Boot](https://spring.io/projects/spring-boot)

**Alternatively** you can use [DevContainers](https://code.visualstudio.com/docs/remote/containers) and [VS Code](https://code.visualstudio.com/) for local development. Opening the project with VS Code will automatically install all the required tools and extensions using DevContainers.

## Getting Started

We use [Make](https://www.gnu.org/software/make/manual/make.html) to automate the build and deployment process. You can run the following command to see the available commands:

```bash
make help
```

The following commands are available:

```bash
help                 💬 This help message :)
all-azure            🏃‍♀️ Run all the things in Azure
start-local          🧹 Setup local Kind Cluster
deploy-local         🚀 Deploy application resources locally
run-local            💿 Run app locally
port-forward-local   ⏩ Forward the local port
test                 🧪 Run tests, used for local development
clean                🧹 Clean up local files
dapr-dashboard       🔬 Open the Dapr Dashboard
dapr-components      🏗️  List the Dapr Components
deploy-azure         🚀 Deploy application resources in Azure
test-azure           🧪 Run tests in Azure
```

## Local Dev Environment Setup

Local environment is setup using [Kind](https://kind.sigs.k8s.io/docs/user/quick-start/). Kind is a tool for running local Kubernetes clusters using Docker container "nodes". Kind was primarily designed for testing Kubernetes itself, but can be used for local development or CI.

### 1. Local Environment Setup

Running following command to setup your local development environment using `Docker` and `Kind`:

```bash
make start-local
```

This script runs the followings:

1. Creates a local Docker registry called `kind-registry` running locally on port 5001.
1. Creates a Kind cluster called `azd-aks` using config file from [kind-cluster-config.yaml](/local/kind-cluster-config.yaml).
1. Connects the registry to the cluster network if not already connected so deployments can access the local registry.
1. Maps the local registry to the cluster
1. Deploys [Redis](https://learn.microsoft.com/en-us/azure/azure-cache-for-redis/) to the cluster using [Helm](https://helm.sh/docs/intro/quickstart/) [chart](https://bitnami.com/stack/redis/helm) to use for different Dapr components (pub/sub, state store, etc).
1. Deploys [Dapr](https://docs.dapr.io/operations/hosting/kubernetes/kubernetes-deploy/) on your local cluster.
1. Deploys [Pub/Sub Broker](https://docs.dapr.io/developing-applications/building-blocks/pubsub/pubsub-overview/) using Redis as the message broker using [redis.yaml](./local/components/redis.yaml) Dapr component.

Your local cluster will be laid out as follows:

![Local](/docs/local.drawio.png)

### 2. Dapr Dashboard & Components

This will open the [Dapr dashboard](/docs/dapr-dashboard.png) in your default browser. This assumes you installed [Dapr CLI](https://docs.dapr.io/getting-started/install-dapr-cli/) in your local machine. 

```bash
make dapr-dashboard
```

You can validate that the setup of the dashboard finished successfully by navigating to <http://localhost:9000>.

To verify the installation of pub/sub broker and other components:

```bash
make dapr-components
```

This will output something similar to the following:

```bash
NAMESPACE  NAME                   TYPE          VERSION  SCOPES  CREATED              AGE
default    money-transfer-pubsub  pubsub.redis  v1               2023-05-10 11:25.24  10m
default    money-transfer-state   state.redis   v1               2023-05-10 11:25.24  10m
```

### 3. Deploy Services to Cluster

By convention, every service under `/src` folder has 2 files:

1. [Dockerfile](/src/public-api-service/Dockerfile) - Dockerfile for building the service image.
1. [local-deploy.sh](/src/public-api-service/local-deploy.sh) - Script file to build, publish and deployment the latest code to local cluster as Docker image.

To deploy all services to the cluster, run the following command under `scripts` folder:

```bash
make deploy-local
```

The deployment script will do the following:

1. Build and publish the Docker image for each service.
1. Deploy the image to the local cluster.
1. Create a Kubernetes service for each service.
1. Register the service with Dapr.

The output of the local deployment of each service will be similar to following:

```bash
🤖  Starting local deployments...


🎖️  Deploying Public API Service


🛖  Releasing version: 2023.05.10.11.38.59


☢️  Attempting to delete existing deployment public-api-service

Error from server (NotFound): deployments.apps "public-api-service" not found

🏗️  Building docker image

[+] Building 0.0s (19/19) FINISHED
 => [internal] load 
 
 ............
 
 => exporting to image                                                                                                                0.0s
 => => exporting layers                                                                                                               0.0s
 => => writing image sha256:0ee9a3ad60209d914d772661a07d64b49c95780b4e43f58457ea86018632d8cd                                          0.0s
 => => naming to localhost:5001/public-api-service:2023.05.10.11.38.59                                                                0.0s

🚚  Pushing docker image to local registry

The push refers to repository [localhost:5001/public-api-service]
.......
0f706090ed95: Layer already exists
a8dd5239cafe: Layer already exists
2023.05.10.11.38.59: digest: sha256:562162f9bfac78b6f234c372748ef20ce2457288a7c625bcb50b2ba5ed751798 size: 1993

🚀  Deploying to cluster

service/public-api-service created
deployment.apps/public-api-service created

🎉  Deployment complete
```

You can check the deployment status of the services:

```bash
kubectl get pods -A
```

You should get an output similar to:

```bash
NAMESPACE            NAME                                            READY   STATUS    RESTARTS       AGE
dapr-system          dapr-dashboard-575df59d4c-mp262                 1/1     Running   0              172m
dapr-system          dapr-operator-676b7df68d-xwzjw                  1/1     Running   0              172m
dapr-system          dapr-placement-server-0                         1/1     Running   1 (171m ago)   172m
dapr-system          dapr-sentry-5f44fd7c9d-gjjcl                    1/1     Running   0              172m
dapr-system          dapr-sidecar-injector-c66df4c49-h645k           1/1     Running   0              172m
default              account-service-655448db67-vdc6n                2/2     Running   0              14m
default              custody-service-5b8656d84c-lgb8j                2/2     Running   0              13m
default              fraud-service-7dfcd56d86-hs7s6                  2/2     Running   0              15m
default              notification-service-7dfbb47b4f-sthln           2/2     Running   0              12m
default              public-api-service-5df9f84648-gb9t2             2/2     Running   0              15m
default              redis-master-0                                  1/1     Running   0              172m
default              redis-replicas-0                                1/1     Running   0              172m
default              redis-replicas-1                                1/1     Running   0              171m
default              redis-replicas-2                                1/1     Running   0              170m
kube-system          coredns-565d847f94-k5nc4                        1/1     Running   0              172m
kube-system          coredns-565d847f94-qx2tn                        1/1     Running   0              172m
kube-system          etcd-azd-aks-control-plane                      1/1     Running   0              172m
kube-system          kindnet-7rhdn                                   1/1     Running   0              172m
kube-system          kindnet-bbk74                                   1/1     Running   0              172m
kube-system          kindnet-nfgrd                                   1/1     Running   0              172m
kube-system          kube-apiserver-azd-aks-control-plane            1/1     Running   0              172m
kube-system          kube-controller-manager-azd-aks-control-plane   1/1     Running   0              172m
kube-system          kube-proxy-6s47w                                1/1     Running   0              172m
kube-system          kube-proxy-7mnzh                                1/1     Running   0              172m
kube-system          kube-proxy-kj8pn                                1/1     Running   0              172m
kube-system          kube-scheduler-azd-aks-control-plane            1/1     Running   0              172m
local-path-storage   local-path-provisioner-684f458cdd-wtmqh         1/1     Running   0              172m
```

### 4. Interacting with the Application

[Public API](/src/public-api-service) is deployed as `Loadbalancer` service type. In local cluster setup, it will not able to get public IP to connect.
Instead, you can access this service locally using the Kubectl proxy tool.

```bash
make port-forward-local
```

While this command is running, you can access the service at <http://localhost:8080>.

```bash
export PUBLIC_API_SERVICE=http://localhost:8080
```

First, you need to create a new account for a user:

```curl
curl -X POST \
  $PUBLIC_API_SERVICE/accounts \
  -H 'Content-Type: application/json' \
  -d '{
    "owner": "A",
    "amount": 100
}'
```

This should return the following response:

```json
{
  "account":
  {
    "owner":"A",
    "amount":100.0
  },
  "message":"Account created for: Owner: A, Amount: 100.0"
}
```

An example request to start a new transfer workflow is:

```curl
curl -X POST \
  $PUBLIC_API_SERVICE/transfers \
  -H 'Content-Type: application/json' \
  -d '{
    "sender": "A",
    "receiver": "B",
    "amount": 100
}'
```

This should return the following response:

```json
{
  "message":"Transfer Request Started: Sender: A, Receiver: B, Amount: 100.0",
  "status":"ACCEPTED",
  "transferId":"14efc"
}
```

You can query the status of a transfer:

```curl
curl -X GET \
  $PUBLIC_API_SERVICE/transfers/{transferId} \
  -H 'Content-Type: application/json'
```

This should return the following response:

```json
{
  "sender":"A",
  "receiver":"B",
  "amount":100.0,
  "transferId":"14efc",
  "status":"COMPLETED"
}
```

### 5. Delete Local Cluster

If you'd like to create a clean state and start over, you can run the following command to delete the local cluster and all the resources associated with it.

```bash
make clean
```

## Implementation Status

- [X] Public API endpoint receives new money transfer request. [TRANSFER(Sender: A, Amount: 100, Receiver:B)]
- [X] Request is published to Redis (pub/sub)
- [X] Deposit workflow starts
  - [X] Fraud service checks the legitimacy of the operation and triggers [VALIDATED(Sender: A, Amount: 100, Receiver:B)]
  - [X] Account service checks if `Sender` has enough funds and triggers [APPROVED(Sender: A, Amount: 100, Receiver: B)]
- [X] Public API can be used to check if there is a confirmation of the money transfer request.

## How To

Following are practical operator guides for common tasks.

### How to Operate Redis

To connect to Redis, you can use the following command:

```bash
kubectl exec -it redis-master-0 -- redis-cli
```

To get Redis password:

```bash
kubectl get secret --namespace default redis -o jsonpath="{.data.redis-password}" | base64 --decode
```

To AUTH with Redis instance for further operations

```bash
AUTH <password>
```

Get all keys in the state store:

```bash
keys *
```

## Azure Deployment

It's possible to deploy this application through [Azure Developer (azd) CLI](https://learn.microsoft.com/en-us/azure/developer/azure-developer-cli/overview). `azd` is responsible for resource provision and application deployment requiring minimal interaction with Azure Portal.

For more information of `azd` CLI and how to [Make your project azd-compatible](https://learn.microsoft.com/en-us/azure/developer/azure-developer-cli/make-azd-compatible?pivots=azd-create)

### Pre-requisites

The following are available in the devContainer environment:

- Azure Developer CLI
- Azure CLI with K8s-extension enabled

The following have to be registered in the azure subscription after signing in:

- AKS-ExtensionManager and AKS-Dapr features
- Kubernetes and ContainerService resource providers

First, make sure you have signed in to Azure CLI:

```bash
az login
```

Then, set the subscription you want to use:

```bash
az account set --subscription <subscription-id>
```

When your subscription is set, you can check the current registration status of the features and resource providers:

```bash
az feature list --query "[?name=='Microsoft.ContainerService/AKS-Dapr' || name=='Microsoft.ContainerService/AKS-ExtensionManager'].{Name:name,State:properties.state}" --output table
```

Then, register the features and resource providers:

```bash
# register AKS-ExtensionManager and AKS-Dapr resource provider
az feature register --namespace "Microsoft.ContainerService" --name "AKS-ExtensionManager"
az feature register --namespace "Microsoft.ContainerService" --name "AKS-Dapr"

# invoke the resource providers for Kubernetes and ContainerService to propagate the registration
az provider register --namespace Microsoft.KubernetesConfiguration
az provider register --namespace Microsoft.ContainerService
```

### 1. Initiate Environment

We are going to use `pubsub-dapr-aks-java` as the environment name for this sample. To initiate an `azd` environment, run the following command:

```bash
azd init --environment pubsub-dapr-aks-java
```

The above command also adds folders to your project to store the environment variables it creates along the deployment process

```txt
├── .azure                     [ For storing Azure configurations]
│   └── <your environment>     [ For storing all environment-related configurations]
│      ├── .env                [ Contains environment variables ]
│      └── config.json         [ Contains environment configuration ]
```

### 2. Resource Provision and Deploy

By just running `azd up`, azd will provision all the resources needed for this sample and deploy the application to the cluster.
This command combines `azd provision` and `azd deploy` to achieve this. 

When issuing the command below, you need to select an Azure Subscription and Location.
Also while installing Dapr post-provision, you need to approve the installation of Dapr components interactively.

```bash
azd up
```

This will output something like this:

```bash
......

Packaging services (azd package)

  (✓) Done: Packaging service account-service
  - Image Hash: sha256:f03ab78c43152afac5263975208301b994f328195741c5ae3cba407854c7722a
  - Image Tag: java--pubsub-dapr-sample/account-service-java--pubsub-dapr-sample:azd-deploy-1684851734
  (✓) Done: Packaging service fraud-service
  - Image Hash: sha256:7aa3ed7fdd27394d7bb9990c363b477f2ab0e7a9487338cc7f45203d00c3df7b
  - Image Tag: java--pubsub-dapr-sample/fraud-service-java--pubsub-dapr-sample:azd-deploy-1684851737
  (✓) Done: Packaging service notification-service
  - Image Hash: sha256:303bb6b3638f512601d99abba03b14ce74041e945abe8c9ba5a09623e57f518c
  - Image Tag: java--pubsub-dapr-sample/notification-service-java--pubsub-dapr-sample:azd-deploy-1684851740
  (✓) Done: Packaging service public-api-service
  - Image Hash: sha256:b4dcc470a2482b80cc97f2c21a1dd704c8a357c10d31c7b7f48c26ba4a39ae4d
  - Image Tag: java--pubsub-dapr-sample/public-api-service-java--pubsub-dapr-sample:azd-deploy-1684851744

  ......

  ✓) Done: Resource group: rg-java--pubsub-dapr-sample
  (✓) Done: Key vault: kv-5jkc2ah3kbvqw
  (✓) Done: Container Registry: cr5jkc2ah3kbvqw
  (✓) Done: AKS Managed Cluster: aks-5jkc2ah3kbvqw
Executing postprovision hook => ./infra/dapr-install.sh
📀 - Post-Provision hook - Installing DAPR AKS...
Merged "aks-5jkc2ah3kbvqw" as current context in /Users/mahmutcanga/.kube/config

......

- Creating java--pubsub-dapr-sample namespace...
namespace/java--pubsub-dapr-sample created

🚀 Deploy Redis on AKS

serviceaccount/redis created
secret/redis created
configmap/redis-configuration created
configmap/redis-health created
configmap/redis-scripts created
service/redis-headless created
service/redis-master created
service/redis-replicas created
statefulset.apps/redis-master created
statefulset.apps/redis-replicas created

🚀 Deploy pub-sub broker component backed by Redis

component.dapr.io/money-transfer-pubsub created

🚀 Deploy state store component backed Redis

component.dapr.io/money-transfer-state created


Deploying services (azd deploy)

  (✓) Done: Deploying service account-service
  - Endpoint: http://None:80, (Service, Type: ClusterIP)

  (✓) Done: Deploying service fraud-service
  - Endpoint: http://None:80, (Service, Type: ClusterIP)

  (✓) Done: Deploying service notification-service
  - Endpoint: http://None:80, (Service, Type: ClusterIP)

  (✓) Done: Deploying service public-api-service
  - Endpoint: http://20.101.13.7, (Service, Type: LoadBalancer)


SUCCESS: Your application was provisioned and deployed to Azure in 16 minutes 19 seconds.

```

The Azure Developer CLI supports various extension points to customize your workflows and deployments. The hooks middleware allows you to execute custom scripts before and after azd commands and service lifecycle events. In our setup, we are using `postprovision` hook to install:

- Dapr
- Redis
- Dapr Redis State Store
- Dapr Redis Pub/Sub

### 3. Interact with the application

At this point, you have everything configured in your Azure environment. From here, you can start interacting with the application.

The environment variables created along the deployment process could be used to interact with the application. You can run the following command to set the environment variables:

```bash
source <(azd env get-values)
```

To check if the API is running and healthy, let's get the public api of the `public-api-service` by running the following command:

```bash
kubectl get service public-api-service -n $AZURE_ENV_NAME
```

This will output something like this:

```bash
NAME                 TYPE           CLUSTER-IP   EXTERNAL-IP    PORT(S)        AGE
public-api-service   LoadBalancer   10.0.54.16   20.8.255.153   80:31319/TCP   2m27s
```

You can use the `EXTERNAL-IP` to access the API. You can run the following command to set the API endpoint available as environment variable:

```bash
PUBLIC_API_SERVICE=$(k get svc public-api-service -n $AZURE_ENV_NAME -o=jsonpath='{.status.loadBalancer.ingress[0].ip}')
```

To check if the API is running and healthy, run the following command:

```bash
curl $PUBLIC_API_SERVICE
```

This will output something like this:

```bash
{"message":"Public API Service Started, version: 1.0.0"}
```

### 4. Test the application

First, you need to create a new account for a user:

```curl
curl -X POST \
  $PUBLIC_API_SERVICE/accounts \
  -H 'Content-Type: application/json' \
  -d '{
    "owner": "A",
    "amount": 100
}'
```

This should return the following response:

```json
{
  "account":
  {
    "owner":"A",
    "amount":100.0
  },
  "message":"Account created for: Owner: A, Amount: 100.0"
}
```

An example request to start a new transfer workflow is:

```curl
curl -X POST \
  $PUBLIC_API_SERVICE/transfers \
  -H 'Content-Type: application/json' \
  -d '{
    "sender": "A",
    "receiver": "B",
    "amount": 100
}'
```

This should return the following response:

```json
{
  "message":"Transfer Request Started: Sender: A, Receiver: B, Amount: 100.0",
  "status":"ACCEPTED",
  "transferId":"14efc"
}
```

You can query the status of a transfer:

```curl
curl -X GET \
  $PUBLIC_API_SERVICE/transfers/{transferId} \
  -H 'Content-Type: application/json'
```

This should return the following response:

```json
{
  "sender":"A",
  "receiver":"B",
  "amount":100.0,
  "transferId":"14efc",
  "status":"COMPLETED"
}
```

### 5. Clean up

This will delete all the resources created by azd, including the resource group created for this sample by running the following command:

```bash
azd down --force --purge
```

This will output something like this:

```bash
Deleting all resources and deployed code on Azure (azd down)
Local application code is not deleted when running 'azd down'.

Deleting your resources can take some time.

  (✓) Done: Deleting resource group: rg-pubsub-dapr-sample-dev

  (✓) Done: Purging key vault: kv-wrxjknyqx2vrk

SUCCESS: Your application was removed from Azure in 11 minutes 13 seconds.
```

### 6. Manual Provisioning

Alternatively, you can provision and deploy the application manually without the hook if you'd require further customization in your workflow.

To deploy all the resources needed for this sample, run the following

```bash
azd provision
```

This will set up a dedicated resource group in your subscription with a clean AKS cluster, a Container Registry for the cluster to pull images from, and a Keyvault for secrets' storage (needed for the AKS deployed through azd).

#### 6.1. Install Dapr and Redis on the Cluster

The variables used on the following commands are generated by azd and stored in `.azure/<your_env_name>/.env`

To use these environment variables, you can run the following command:

```bash
source <(azd env get-values)
```

```bash
# Install DAPR on AKS Cluster
az k8s-extension create --cluster-type managedClusters \
  --cluster-name $AZURE_AKS_CLUSTER_NAME \
  --resource-group $AZURE_RESOURCE_GROUP_NAME \
  --name myDaprExtension \
  --extension-type Microsoft.Dapr
```

To connect to the cluster and run other steps, update your current context of `kubectl` by running the following command:

```bash
az aks get-credentials --resource-group $AZURE_RESOURCE_GROUP_NAME --name $AZURE_AKS_CLUSTER_NAME
```

To check if Dapr is installed, run the following command:

```bash
kubectl get pods -n dapr-system
```

This will output something like this:

```bash
NAME                                    READY   STATUS    RESTARTS   AGE
dapr-dashboard-7798c78c74-4ddw5         1/1     Running   0          5m9s
dapr-operator-8955455bf-6qs79           1/1     Running   0          5m9s
dapr-placement-server-0                 1/1     Running   0          5m9s
dapr-sentry-69c8464dc9-6rtkf            1/1     Running   0          5m9s
dapr-sidecar-injector-fcbc4b979-wcjmj   1/1     Running   0          5m9s
```

We will deploy the application to its own namespace. Run following command to create a namespace for the application:

```bash
kubectl create namespace $AZURE_ENV_NAME
```

Check if the namespace is created:

```bash
kubectl get namespace
```

This will output something like this:

```bash
NAME                                  STATUS   AGE
dapr-system                           Active   6m48s
default                               Active   122m
gatekeeper-system                     Active   121m
java-pubsub-dapr-sample-dev           Active   25s   // <--- This is the namespace we created using $AZURE_ENV_NAME
kube-node-lease                       Active   122m
kube-public                           Active   122m
kube-system                           Active   122m
```

We are using an in-cluster Redis instance for the pub-sub and state store components. To deploy Redis, run the following command:

```bash
kubectl apply -f ./infra/redis.yaml --namespace $AZURE_ENV_NAME --wait=true
```

You can check the Redis installation by running the following command:

```bash
kubectl get pods --namespace $AZURE_ENV_NAME
```

This will output something like this:

```bash
NAME               READY   STATUS    RESTARTS      AGE
redis-master-0     1/1     Running   0             2m50s
redis-replicas-0   0/1     Running   3 (28s ago)   2m50s
```

#### 6.2. Deploy PubSub and State Store components

Now, we have an in-cluster Redis instance running. We can deploy the pub-sub and state store components for Dapr.

##### 6.2.1. Deploy pub-sub broker component backed by Redis

```bash
kubectl apply -f ./local/components/pubsub.yaml --wait=true --namespace $AZURE_ENV_NAME
```

You can check the pub-sub broker installation by running the following command:

```bash
dapr components -k --namespace $AZURE_ENV_NAME
```

This will output something like this:

```bash
NAMESPACE                            NAME                   TYPE          VERSION  SCOPES  CREATED              AGE
java--pubsub-dapr-sample-dev  money-transfer-pubsub  pubsub.redis  v1               2023-05-22 16:55.50  56s
```

##### 6.2.2. Deploy state store component backed Redis

```bash
kubectl apply -f ./local/components/state.yaml --wait=true --namespace $AZURE_ENV_NAME
```

You can check the state store component installation by running the following command:

```bash
dapr components -k --namespace $AZURE_ENV_NAME
```

This will output something like this:

```bash
NAMESPACE                            NAME                   TYPE          VERSION  SCOPES  CREATED              AGE
java--pubsub-dapr-sample-dev  money-transfer-pubsub  pubsub.redis  v1               2023-05-22 16:55.50  2m
java--pubsub-dapr-sample-dev  money-transfer-state   state.redis   v1               2023-05-22 16:57.46  13s
```

##### 6.2.3. Deploy microservices

Previously, we have setup our cluster, deployed Dapr and its components. Now, we can deploy the microservices.

Before you start the deployment, you will need to login to Azure Container Registry (ACR) to be able to push the images. To do so, run the following command.
This command logs in to an Azure Container Registry through the Docker CLI. Docker must be installed on your machine. Once done, use 'docker logout <registry url>' to log out. (If you only need an access token and do not want to install Docker, specify '--expose-token').

```bash
az acr login --name $AZURE_CONTAINER_REGISTRY_NAME
```

This will output something like this:

```bash
Login Succeeded
```

This command deploys all services defined in `azure.yaml` file in the namespace defined by the environment name, based on their individual deployment under `manifests` folder by `azd` convention.

```bash
azd deploy
```

This will output something like this:

```bash
Deploying services (azd deploy)

  (✓) Done: Deploying service account-service
  - Endpoint: http://None:80, (Service, Type: ClusterIP)

  (✓) Done: Deploying service fraud-service
  - Endpoint: http://None:80, (Service, Type: ClusterIP)

  (✓) Done: Deploying service notification-service
  - Endpoint: http://None:80, (Service, Type: ClusterIP)

  (✓) Done: Deploying service public-api-service
  - Endpoint: http://20.229.248.100, (Service, Type: LoadBalancer)


SUCCESS: Your application was deployed to Azure in 12 minutes 13 seconds.
You can view the resources created under the resource group rg-java--pubsub-dapr-sample-dev in Azure Portal:
https://portal.azure.com/#@/resource/subscriptions/a3ed6c04-563f-4855-ac84-bdf1e5fbc3fc/resourceGroups/rg-java--pubsub-dapr-sample-dev/overview
```
