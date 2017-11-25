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
docker build -t projectriff/riff-demo-ui .
```

#### To apply to k8s
```
kubectl apply -f config
```
This will deploy a pod with the ui server container using the image above.

Open the k8s service in the browser by running:
```
minikube service ui
```

#### Debugging
- websocket connect and disconnect messages should appear in the browser console and server log.
- use the `/test.html` endpoint to see the old mouse-event test page and test replica counts
- use the `/echo` endpoint to see the environment
