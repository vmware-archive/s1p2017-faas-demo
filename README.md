# Spring One 2017 demo of vote counting using riff

This demo consists of three functions and a UI to collect votes and display the results.

**NOTE**: Because of riff [issue #553](https://github.com/projectriff/riff/issues/553), we recommend running this demo with riff version 0.0.5, following the instructions from commit [cf388c3](https://github.com/projectriff-samples/s1p2017-faas-demo/tree/cf388c3cd8dd1ad9f570ebf6ab6edfa17a9888ac) of this repo, and using the 0.0.5 riff helm chart together with the [v0.0.5 CLI](https://github.com/projectriff/riff/releases/tag/v0.0.5).

```
helm install --name control --namespace riff-system projectriff/riff --version 0.0.5 --set rbac.create=false --set httpGateway.service.type=NodePort
```

---

![votes demo image](s1p-demo-votes.png "Votes Demo")

The vote bubbles on the top of the UI page collects votes and posts them via the HTTP gateway to the _votes_ topic.
The votes are processed by the `vote-counter` function and written to Redis.
The `vote-stream-processor` also reads the _votes_ topic and processes the stream of votes with two windowing operations.
One window for counts every two seconds and one for counts every 60 seconds.
The results are written to a _function-replicas_ topic read by the `redis-writer` function which writes the results to Redis.
The UI then collects the windowing results and shows it at the bottom of the page.

## Running the Demo

### install riff

Follow the instructions at [projectriff.io](https://projectriff.io/docs/getting-started-on-minikube/) to install riff onto minikube or GKE.
The demo assumes service names from `helm install -n projectriff...` or a recent `make dev-setup`.

### install redis

```
helm install -n counters stable/redis --set serviceType=NodePort --set persistence.enabled=false
```

### deploy functions

```
riff apply -f functions/vote-counter/
riff apply -f functions/redis-writer/
riff apply -f functions/vote-stream-processor/
```

### deploy UI

```
# minikube - nodeport
kubectl apply -f ui/minikube/

# GKE - loadbalancer
kubectl apply -f ui/gke-rbac/
```

### browse UI

```
# minikube - nodeport  
minikube service votes-ui

# GKE - loadbalancer: browse to EXTERNAL-IP
kubectl get votes-ui
```

## build
This is only required if you are making changes to the functions or UI.
The steps below assume minikube. To build and deploy via docker hub, replace "projectriff" 
with your own docker id in the steps below, and adjust the image names in the yaml configs.
``` 
eval $(minikube docker-env)

pushd functions/vote-stream-processor ; ./mvnw -DskipTests clean package ; popd
riff build -f functions/vote-stream-processor/ -v 0.2.1 -u projectriff
riff build -f functions/vote-counter/ -v 0.2.1 -u projectriff
riff build -f functions/redis-writer/ -v 0.2.1 -u projectriff
docker build -t projectriff/riff-demo-ui:0.2.1 ui
```

## tear down

### delete functions and UI

Use `kubectl delete -f ...` instead of `kubectl apply -f ...` for the functions and UI above.


### delete redis and riff

```
helm delete counters --purge
helm delete projectriff --purge
```
