const express = require('express')
const app = express()
const server = require('http').Server(app)
const util = require('util')
const bodyParser = require('body-parser')

// object matching destructuring inspired by https://apex.github.io/up/#getting_started
const { 
  PORT = 8080,
  REDIS_HOST,
  REDIS_PORT,
  REDIS_PASSWORD
} = process.env

const redisLib = require("redis")
const redisOpts = {host:REDIS_HOST, port:REDIS_PORT, auth_pass:REDIS_PASSWORD}

// redisWatch client is just for watching events on demo keys
const redisWatch = redisLib.createClient(redisOpts)
redisWatch.config('SET', 'notify-keyspace-events', 'KEA')
redisWatch.on('error', (err) => { console.log(`redisWatch error: ${err}`); })
redisWatch.psubscribe('__keyspace*__:demo*')

// chrome socket.io seems to leak connections 
redisWatch.setMaxListeners(500)

// redisDB client is used to get/set values of demo keys
const redisDB = redisLib.createClient(redisOpts)
redisDB.on('error', (err) => { console.log(`redisDB error: ${err}`); })

const io = require('socket.io')(server)

io.on('connection', (socket) => { 

  redisWatch.on('pmessage', redisEvent)
  socket.on('disconnect', function(){
    redisWatch.removeListener('pmessage', redisEvent);
    console.log(`disconnect ${socket.id}`)
  })

  console.log(`connect ${socket.id}`)
  socket.on('logevent', (evt) => { console.log(evt); })

  socket.on('mouseevent', (evt) => { 
    redisDB.mset('demo:x', evt.x, 'demo:y', evt.y, (err) => {
      socket.emit('echoevent', evt); 
    })
  })
  
  function redisEvent(pat, ch, msg) { 
    redisDB.mget('demo:x', 'demo:y', (err, vals) => {
      socket.emit('redisevent', {x:vals[0], y:vals[1]})
    })
  }
})

app.use('/', bodyParser.text());

// handle event from kafka riff_function_replicas topic via sidecar
app.post('/', (req, res) => {
  const event = safeParseJSON(req.body);
  const retval = "riff_function_replicas event: " + JSON.stringify(event);
  console.log(retval)
  res.type("text/plain")
  res.status(200).send(retval)

  function safeParseJSON(s) {
    try {
      return JSON.parse(s);
    } catch(err) {
      console.log('error parsing event JSON: ' + s);
      return {};
    }
  }
})

// use /echo for debugging with request counter middleware
var reqcnt = 0;
app.use((req, res, next) => { reqcnt++; next(); })
app.use('/echo', (req, res) => { res.send(
{ reqcnt: reqcnt,
  env: process.env,
  method: req.method,
  originalUrl: req.originalUrl,
  headers: req.headers,
  query: req.query,
  params: req.params,
  body: req.body,
  cookies: req.cookies }
)})

// default to serving static for all other paths
app.use(express.static('public'))

// must listen on server (not app) for socket.io to work
server.listen(PORT, () => console.log(`UI listening on port ${PORT}`))


// --------------------------
// graceful shutdown (credit)
// https://medium.com/@becintec/building-graceful-node-applications-in-docker-4d2cd4d5d392
// https://medium.com/@gchudnov/trapping-signals-in-docker-containers-7a57fdda7d86
var signals = {
  'SIGHUP': 1,
  'SIGINT': 2,
  'SIGTERM': 15
}

// Do any necessary shutdown logic for our application here
const shutdown = (signal, value) => {
  redisWatch.quit();  
  redisDB.quit();  
  server.close(() => {
    console.log(`server shutdown by ${signal} with value ${value}`)
    process.exit(128 + value)
  })
}

Object.keys(signals).forEach((signal) => {
  process.on(signal, () => {
    shutdown(signal, signals[signal])
  })
})