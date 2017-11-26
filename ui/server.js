const express = require('express')
const app = express()
const server = require('http').Server(app)
const util = require('util')
const fetch = require('node-fetch')


// object matching destructuring inspired by https://apex.github.io/up/#getting_started
const { 
  PORT = 8080,
  REDIS_HOST,
  REDIS_PORT,
  REDIS_PASSWORD,
  HTTP_GATEWAY_SERVICE_HOST,
  HTTP_GATEWAY_SERVICE_PORT
} = process.env

// correlate fetch requests
var fetch_count = 0

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

  socket.on('mouseevent', (evt) => { 
    redisDB.mset('demo:x', evt.x, 'demo:y', evt.y, (err) => {
      if (err) console.log('Error writing mouseevent to redis' + err);
      socket.emit('echoevent', evt); 
    })
  })

  socket.on('vote', (evt) => {
//    redisDB.hincrby("demo:votes", evt, 1, (err) => {
//      if (err) console.log('Error writing votes to redis' + err);
//    })
    const endpoint = `http://${HTTP_GATEWAY_SERVICE_HOST}:${HTTP_GATEWAY_SERVICE_PORT}/requests/votes`
    const body = `{"${evt}":1, "_command":"increment"}`
    fetch_count++
    console.log('fetch', fetch_count, endpoint, body)
    fetch(endpoint, {
      method: 'POST',
      body: body,
      headers: { 'Content-Type': 'text/plain' }
    })
    .then(res => console.log(fetch_count, res.status))
    .catch(err => console.log(fetch_count, err))
  })

  socket.on('testscale', (evt) => {
    console.log('testscale event: ', evt)
    for (fn in evt) {
      if (evt[fn] == "delete") {
        redisDB.hdel('demo:function-replicas', fn, (err) => {
          if (err) console.log('Error deleting function-replicas from redis' + err);
        })
      } else {
        redisDB.hset('demo:function-replicas', fn, evt[fn], (err) => {
          if (err) console.log('Error writing function-replicas to redis' + err);
        })
      }
    }
  })

  function redisEvent(pat, ch, msg) { 
    const key = ch.replace(/[^:]*:(.*)/,'$1')
    switch (key) {
    case 'demo:function-replicas':
    case 'demo:votes':
    redisDB.hgetall(key, (err, vals) => {
        if (err) return;
        socket.emit(key, vals)
      })
      break;
    case 'demo:x':
    case 'demo:y':
      redisDB.get(key, (err, val) => {
        if (err) return;
        socket.emit(key, val)
      })
      break;
    }
  }
})

// use /echo for debugging with request counter middleware
var reqcnt = 0;
app.use((req, res, next) => { reqcnt++; next(); })
app.use('/echo', (req, res) => {
  res.send('<pre>'+util.inspect(
    { reqcnt: reqcnt,
      env: process.env,
      method: req.method,
      originalUrl: req.originalUrl,
      headers: req.headers,
      query: req.query,
      params: req.params,
      body: req.body,
      cookies: req.cookies },
    { depth:2 }
  )+'</pre>')
})

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