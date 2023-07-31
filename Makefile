SHELL := /bin/bash

VERSION := 0.0.1
BUILD_INFO := Manual build

ENV_FILE := .env
ifeq ($(filter $(MAKECMDGOALS),config clean),)
	ifneq ($(strip $(wildcard $(ENV_FILE))),)
		ifneq ($(MAKECMDGOALS),config)
			include $(ENV_FILE)
			export
		endif
	endif
endif

.PHONY: help lint image push build run
.DEFAULT_GOAL := help

help: ## 💬 This help message :)
	@grep -E '[a-zA-Z_-]+:.*?## .*$$' $(firstword $(MAKEFILE_LIST)) | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

####### LOCAL #############
start-local: ## 🧹 Setup local Kind Cluster
	@echo -e "\e[34m$@\e[0m" || true
	@./scripts/start-local-env.sh

deploy-local: ## 🚀 Deploy application resources locally
	@echo -e "\e[34m$@\e[0m" || true
	@./scripts/deploy-services-local.sh
	@echo -e "\e[34mYOU WILL NEED TO START A NEW TERMINAL AND RUN  make test\e[0m" || true

run-local: clean start-local deploy-local ## 💿 Run app locally

port-forward-local: ## ⏩ Forward the local port
	@echo -e "\e[34m$@\e[0m" || true
	@kubectl port-forward service/public-api-service 8080:80 --pod-running-timeout=3m0s

dapr-dashboard: ## 🔬 Open the Dapr Dashboard
	@echo -e "\e[34m$@\e[0m" || true
	@dapr dashboard -k -p 9000

dapr-components: ## 🏗️  List the Dapr Components
	@echo -e "\e[34m$@\e[0m" || true
	@dapr components -k

test-local: ## 🧪 Run tests, used for local development
	@echo -e "\e[34m$@\e[0m" || true
	@./scripts/test.sh

test-e2e: ## 🧪 Run end to end tests
	@echo -e "\e[34m$@\e[0m" || true
	@cd test/e2e-test && ./gradlew run

####### AZURE #############
test-azure: ## 🧪 Run tests in Azure
	@echo -e "\e[34m$@\e[0m" || true
	@./scripts/test.sh --azure

clean: ## 🧹 Clean up local files
	@echo -e "\e[34m$@\e[0m" || true
	@kind delete cluster --name azd-aks
	@docker rm kind-registry -f