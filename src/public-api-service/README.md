# Public API

This is the public API for starting money transfers.

## Deploy to Local Cluster

Assuming a local cluster is running, you can deploy the service to it using the following command:

```bash
./local-deploy.sh
```

## Create New Transfer

```bash
curl -X POST \
  http://localhost:8080/transfers \
  -H 'Content-Type: application/json' \
  -d '{
    "sender": "A",
    "receiver": "B",
    "amount": 100
}'
```