module.exports = (vote) => {

// only count valid votes
if (!{boot:1, framework:1, reactor:1, riff:1}.hasOwnProperty(vote)) {
  return '_boo'
}

const redisOpts = {
  host:process.env.COUNTERS_REDIS_SERVICE_HOST,
  port:process.env.COUNTERS_REDIS_SERVICE_PORT,
  auth_pass:process.env.REDIS_PASSWORD
}

const redisDB = require('redis').createClient(redisOpts)

redisDB.on('error', (err) => { console.log(`redisDB error: ${err}`); })

redisDB.hincrby("demo:votes", vote, 1, (err) => {
  if (err) console.log('Error writing votes to redis' + err);
})

return vote;
} 
