#!/bin/bash

#./install-redis
./deploy-functions
./deploy-ui
sleep 3
open `minikube service ui --url`/demo.html

