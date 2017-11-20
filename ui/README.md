# UI server
nodejs socket.io server backed by redis  
designed for real-time demos

#### To run standalone locally
```
cd ui
npm install
node server
```
Point your browser to http://localhost:8080.
If redis is working the display will update with the mouse position as you move over the yellow box.

The server looks for redis using environment variables `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD`.
Defaults are localhost, 6379, and no auth.  

#### To build docker image
```
docker build -t jldec/sk8s-ui .
```

#### To apply to k8s
```
kubectl apply -f config
```
This will deploy a pod with the ui server container using the image above as well as the function sidecar.

Open the K8s service in the browser by running:
```
minikube service ui
```

#### Debugging
- websocket connect and disconnect messages should appear in the browser console when the root page is loaded  
- use the `/echo` endpoint to see the environment
