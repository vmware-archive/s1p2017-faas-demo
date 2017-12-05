#!/bin/bash

#./install-redis
./deploy-functions
./deploy-ui
sleep 10
minikube service ui

