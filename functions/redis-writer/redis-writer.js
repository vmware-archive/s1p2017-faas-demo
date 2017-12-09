const redisOpts = {
  host:process.env.COUNTERS_REDIS_SERVICE_HOST,
  port:process.env.COUNTERS_REDIS_SERVICE_PORT,
  auth_pass:process.env.REDIS_PASSWORD
}

const redisDB = require('redis').createClient(redisOpts)

redisDB.on('error', (err) => { console.log(`redisDB error: ${err}`); })

module.exports = (input_object) => {

const util = require('util');

var input = input_object;

if (typeof input === 'string') {
  try { input = JSON.parse(input) }
  catch(err) { return `Error parsing input string "${input}": ${err}`; }
}

if (Array.isArray(input)) { input = input[0] }
if (typeof input !== 'object') return "Error, input is not a JSON object.";

var { DEFAULT_HASH_KEY, DEFAULT_COMMAND } = process.env

var op = input._operation || DEFAULT_COMMAND 
delete input._operation; 

var hash = input._hash || DEFAULT_HASH_KEY
delete input._hash;

if (input._list) {
  const key = input._list
  delete input._list
  redisDB.rpush(key, JSON.stringify(input), redisErr)
}
else 
{
  for (key in input) {
    if (/^_/.test(key)) continue;

    if (hash) {
      if (op == 'increment') {
        redisDB.hincrby(hash, key, input[key], redisErr)
      } else {
        redisDB.hset(hash, key, input[key], redisErr)
      }
    } else {
      if (op == 'increment') {
        redisDB.incrby(key, input[key], redisErr)
      } else {
        redisDB.set(key, input[key], redisErr)
      }
    }
  }
}

function redisErr(err) { if (err) { console.log(err) }}

return util.inspect(input_object); // return complete input_object as a string for observability
} 
