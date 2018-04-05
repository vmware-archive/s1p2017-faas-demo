// circular buffer of vote-counts every 2 seconds, recycled every 60s
const aggregates = {
  boot: new Array(30).fill(0),
  framework: new Array(30).fill(0),
  reactor: new Array(30).fill(0),
  riff: new Array(30).fill(0),
}

module.exports = (input, output) => {
  // aggregates index scrolls from 0 to 29 and then back to 0
  var idx = 0;

  input.on('data', vote => {
    if ({boot:1, framework:1, reactor:1, riff:1}.hasOwnProperty(vote)) {
       (aggregates[vote][idx])++;
    }
  });

  // every 2 seconds, publish tallies from the last 2 seconds, and total from the last 60s.
  setInterval(() => {
    var ts = Date.now()

    var logdata = {
      "_list":"demo:votes-log",
      "_time":ts,
      boot: aggregates.boot[idx],
      framework: aggregates.framework[idx],
      reactor: aggregates.reactor[idx],
      riff: aggregates.riff[idx]
    }

    if (logdata.boot == 0) delete logdata.boot
    if (logdata.framework == 0) delete logdata.framework
    if (logdata.reactor == 0) delete logdata.reactor
    if (logdata.riff == 0) delete logdata.riff

    output.write(JSON.stringify(logdata))

    var windowdata = {
      "_list":"demo:votes-windows",
      "_time":ts,
      boot: aggregates.boot.reduce((s,v) => s+v,0),
      framework: aggregates.framework.reduce((s,v) => s+v,0),
      reactor: aggregates.reactor.reduce((s,v) => s+v,0),
      riff: aggregates.riff.reduce((s,v) => s+v,0) 
    }

    output.write(JSON.stringify(windowdata))

    idx = (idx + 1) % 30
    aggregates.boot[idx] = 0
    aggregates.framework[idx] = 0
    aggregates.reactor[idx] = 0
    aggregates.riff[idx] = 0

  }, 2000);

  input.on('end', () => {
    output.end();
  });

};
module.exports.$interactionModel = 'node-streams';
