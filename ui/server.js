const express = require('express')
const app = express()
const server = require('http').Server(app)
const util = require('util')
const fetch = require('node-fetch')
const EventEmitter = require('events')

// object matching destructuring inspired by https://apex.github.io/up/#getting_started
const { 
  PORT = 8080,
  REDIS_HOST,
  REDIS_PORT,
  REDIS_PASSWORD,
  HTTP_GATEWAY_SERVICE_HOST,
  HTTP_GATEWAY_SERVICE_PORT
} = process.env

var vote_post_cnt = 0  // to correlate fetch requests with responses
const vote_post_endpoint = `http://${HTTP_GATEWAY_SERVICE_HOST}:${HTTP_GATEWAY_SERVICE_PORT}/requests/votes`

if (!HTTP_GATEWAY_SERVICE_HOST) { console.log("WARNING: No HTTP_GATEWAY - writing votes to redis directly.") }

// circular buffer of vote-counts every 2 seconds, recycled every 60s
const aggregates = {
  boot: new Array(30).fill(0),
  framework: new Array(30).fill(0),
  reactor: new Array(30).fill(0),
  riff: new Array(30).fill(0),
}

// aggregates index scrolls from 0 to 30 and then back to 0
var idx = 0;
var tStart = Date.now()
const em = new EventEmitter();

// every 2 seconds, publish tallies from the last 2 seconds, and total from the last 60s.
setInterval(() => {
//function doit() {
  em.emit('logevent', {
    t: Date.now() - tStart,
    "min": {
      boot: aggregates.boot.reduce((s,v) => s+v),
      framework: aggregates.framework.reduce((s,v) => s+v),
      reactor: aggregates.reactor.reduce((s,v) => s+v),
      riff: aggregates.riff.reduce((s,v) => s+v) },
    "sec": {
      boot: aggregates.boot[idx],
      framework: aggregates.framework[idx],
      reactor: aggregates.reactor[idx],
      riff: aggregates.riff[idx] }
  })
  idx = (idx + 1) % 30
  aggregates.boot[idx] = 0
  aggregates.framework[idx] = 0
  aggregates.reactor[idx] = 0
  aggregates.riff[idx] = 0
//}
}, 2000)

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
  em.on('logevent', logEvent)
  
  socket.on('disconnect', function(){
    redisWatch.removeListener('pmessage', redisEvent)
    em.removeListener('logevent', logEvent)
    console.log(`disconnect ${socket.id}`)
  })

  console.log(`connect ${socket.id}`)

  function logEvent(evt) {
    socket.emit('logevent', evt)
  }

  socket.on('mouseevent', (evt) => { 
    redisDB.mset('demo:x', evt.x, 'demo:y', evt.y, (err) => {
      if (err) console.log('Error writing mouseevent to redis' + err);
      socket.emit('echoevent', evt); 
    })
  })

  socket.on('vote', (evt) => {
    (aggregates[evt][idx])++;
    //doit()

    if (HTTP_GATEWAY_SERVICE_HOST) {
      const body = `{"${evt}":1, "_command":"increment"}`
      vote_post_cnt++
      console.log('fetch', vote_post_cnt, vote_post_endpoint, body)
      fetch(vote_post_endpoint, {
        method: 'POST',
        body: body,
        headers: { 'Content-Type': 'text/plain' }
      })
      .then(res => console.log(vote_post_cnt, res.status))
      .catch(err => console.log(vote_post_cnt, err))
    } else {
      redisDB.hincrby("demo:votes", evt, 1, (err) => {
        if (err) console.log('Error writing votes to redis' + err);
      })
    }
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