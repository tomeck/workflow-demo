var doWorkflow = require('../amqplib-rpc').doWorkflow
var amqplib = require('amqplib')

var amqpConnString = 'amqp://guest:guest@localhost:5672'
var open = amqplib.connect(amqpConnString);

open.then(function(connection) {
    var content = 'Hello from Node'
    var workflow = {remainWkflw:[ {Name:'B', NextAddr:'processed'}, {Name:'C', NextAddr:'requests'}, {Name:'D', NextAddr:'processed'}, {Name:'Z', NextAddr:'reply-to'}]}
    var opts = { exchangeName : 'wf-demo'}

    // RPC request: sends to routing-key (2nd param) of exchange specified in opts (above)
    // Specifies the workflow to execute
    // Waits for the workflow to complete (i.e. the last step must have NextAddr:reply-to)
    doWorkflow(connection, 'requests', content, JSON.stringify(workflow), opts, function (err, replyMessage) {
      if (err) throw err
      console.log(replyMessage.content.toString())

      process.exit()
    })
  })

