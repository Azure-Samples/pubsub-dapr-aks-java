#!/bin/bash

set -o errexit

azure_deployment=0
FRONT_END_IP=localhost:8080

usage() {
    echo ""
    echo "usage: ./test.sh [--azure]"
    echo ""
    echo "  --azure       boolean     Optional. Test in Azure"
    echo ""
}

failed() {
    printf "💥 Script failed: %s\n\n" "$1"
    exit 1
}

# parse parameters

if [ $# -gt 1 ]; then
    usage
    exit 1
fi

while [ $# -gt 0 ]
do
    name="${1}"
    case "$name"  in
        --azure) azure_deployment=1;;
        --) shift;;
    esac
    shift;
done

if [ $azure_deployment -eq 1 ]; then
  source <(azd env get-values)
  FRONT_END_IP=$(kubectl get service public-api-service -o jsonpath='{.status.loadBalancer.ingress[0].ip}' --namespace $AZURE_ENV_NAME)
fi

transfer=$(curl -X POST \
  http://${FRONT_END_IP}/transfers \
  -H 'Content-Type: application/json' \
  -d '{
    "sender": "A",
    "receiver": "B",
    "amount": 100
}' | jq)

echo $transfer

transferId=$(echo $transfer | jq -r '.transferId')
echo "TransferId: $transferId"

curl -X GET \
  http://${FRONT_END_IP}/transfers/${transferId} \
  -H 'Content-Type: application/json' | jq