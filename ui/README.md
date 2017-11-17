# UI server
nodejs socket.io server backed by redis  
designed for real-time demos

#### To run standalone locally
```
npm install
node server
```
Then browse to http://localhost:3000

The server looks for redis using environment variables (defaults are localhost, 6379, and no auth)   
REDIS_HOST  
REDIS_PORT  
REDIS_PAASWORD  

#### To build docker image
```
docker build -t jldec/sk8s-ui .
```

#### To apply to k8s
```
kc apply -f config
```

#### Debugging
- websocket connect and disconnect messages should appear in the browser console when the root page is loaded  
- use the `/echo` endpoint to see the environment

