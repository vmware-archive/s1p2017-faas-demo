# Demo UI server
nodejs socket.io server backed by redis

The server looks for redis using environment variables `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD`.
Defaults are localhost, 6379, and no auth.

Current votecounts are displayed by monitoring the `demo:votes` hash in redis.
Function replica counts are monitored from redis hash `demo:function-replicas`.
The log and window displays use redis lists `demo:votes-log` and `demo:votes-windows`.
These lists accumulate JSON aggregates of total votes over the last 2s and the last 60s.

Votes are posted to the votes topic in riff via the http-gateway service configured
at `HTTP_GATEWAY_SERVICE_HOST` and `HTTP_GATEWAY_SERVICE_PORT` (optionally prefixed with
a helm deploy name). If no gateway is found, the ui server assumes that it is running
standalone (outside minikube) with just redis, votes are incremented in redis directly
instead of going through the gateway, and the aggregate log and windows are computed
every 2s by the ui server.

#### To run the demo UI locally standalone, with just redis
```
cd ui
npm install
node server
```

#### To run the demo UI locally with redis and the http-gateway in minikube
```
source scripts/localenv # this confgures the environment for redis and http-gateway
node server
```

#### To install redis on minikube
```
helm init
helm install -n counters stable/redis --set serviceType=NodePort --set persistence.enabled=false
```

#### To install the demo on riff using docker images
first install riff, then change to the demo directory
```
pushd functions/vote-counter ; riff apply ; popd
pushd functions/redis-writer ; riff apply ; popd
pushd functions/vote-stream-processor ; riff apply ; popd
pushd ui ; kubectl apply -f config ; popd
```

#### To build the demo docker images with minikube's docker environment
change to the demo directory
```
eval $(minikube docker-env)
pushd functions/vote-counter ; riff build -v 0.0.3 ; popd
pushd functions/redis-writer ; riff build -v 0.0.3; popd
pushd functions/vote-stream-processor ; ./mvnw -DskipTests clean package ; riff build -v 0.0.3 ; popd
pushd ui ; docker build -t projectriff/riff-demo-ui:0.0.4 . ; popd
```

#### Debugging
- websocket connect and disconnect messages should appear in the browser console and server log.
- use the `/test.html` endpoint to simulate function replica counts when running standalone without riff
- use the `/echo` endpoint to see the environment in the server
