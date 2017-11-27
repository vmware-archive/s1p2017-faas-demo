# Demo UI server
nodejs socket.io server backed by redis

The server looks for redis using environment variables `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD`.
Defaults are localhost, 6379, and no auth.

Votes are posted to the votes topic in Riff via the http-gateway service configured 
at `HTTP_GATEWAY_SERVICE_HOST` and `HTTP_GATEWAY_SERVICE_PORT`. Current votecounts are
displayed by monitoring the `demo:votes` hash in redis.

Function replica counts are monitored from redis hash `demo:function-replicas`.

#### To run locally with redis and http-gateway in k8s
```
cd ui
npm install

source scripts/localenv # this confgures the environment for redis and http-gateway
node server
```

#### To install redis in k8s
change to the demo directory
```
helm init
scripts/install-redis
```

#### To build and install the redis-writer functions in k8s
change to the demo/functions/redis-writer directory
```
./mvnw clean package
kubectl apply -f .
kubectl apply -f function-replicas-writer
```

#### To build the ui docker image and install in k8s
change to the demo/ui directory.
```
docker build -t projectriff/riff-demo-ui .
kubectl apply -f config
```
Open the k8s service in the browser by running:
```
minikube service ui
```

#### Debugging
- websocket connect and disconnect messages should appear in the browser console and server log.
- tail the server log in k8s using `kubetail ui`
- use the `/test.html` endpoint to see the old mouse-event test page and to test visualizing changes in function replica counts
- use the `/echo` endpoint to see the environment in the server
