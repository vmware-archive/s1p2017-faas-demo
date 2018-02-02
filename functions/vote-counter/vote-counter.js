const redisOpts = {
  host:process.env.COUNTERS_REDIS_SERVICE_HOST,
  port:process.env.COUNTERS_REDIS_SERVICE_PORT,
  auth_pass:process.env.REDIS_PASSWORD
}

const redis = require('redis')
const key = 'demo:votes'

let redisDB
module.exports = fn

fn.$init = () => {
  console.log('redisDB init')
  redisDB = redis.createClient(redisOpts)
  redisDB.on('error', redisErr)
}

fn.$destroy = () => {
  console.log('redisDB quit')
  redisDB.removeListener('error', redisErr)
  redisDB.quit();
}

function fn(vote) {
  return new Promise((resolve, reject) => {

    // only count valid votes
    if (!{boot:1, framework:1, reactor:1, riff:1}.hasOwnProperty(vote)) {
      return resolve('_boo')
    }

    redisDB.hincrby(key, vote, 1, (err) => {
      return (err ? reject(err) : resolve(vote))
    })
  })
}

function redisErr(err) {
  // shut down function?
  console.log(`redisDB error: ${err}`)
}
