const express = require('express')
const app = express()
const server = require('http').Server(app);
const util = require('util')

// object matching destructuring inspired by https://apex.github.io/up/#getting_started
const { 
  PORT = 3000,
  REDIS_HOST,
  REDIS_PORT,
  REDIS_PASSWORD
} = process.env

const redisLib = require("redis")

const redisWatch = redisLib.createClient({host:REDIS_HOST, port:REDIS_PORT, auth_pass:REDIS_PASSWORD})
redisWatch.config('SET', 'notify-keyspace-events', 'KEA')
redisWatch.on('error', (err) => { console.log(`redisWatch error: ${err}`); })
redisWatch.psubscribe('__keyspace*__:demo*')

// chrome socket.io seems to leak connections 
redisWatch.setMaxListeners(500)

const redisDB = redisLib.createClient({host:REDIS_HOST, port:REDIS_PORT, auth_pass:REDIS_PASSWORD})
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



// TODO: release event handlers on disconnect?

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

// default to static for everything else
app.use(express.static('public'))

// have to listen on server (not app) for socket.io to work
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