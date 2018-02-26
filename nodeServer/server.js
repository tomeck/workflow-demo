var express = require("express");
var bodyParser = require("body-parser");
var app = express();

var doWorkflow = require('../amqplib-rpc').doWorkflow
var amqplib = require('amqplib')

var amqpConnString = 'amqp://guest:guest@localhost:5672'
var open = amqplib.connect(amqpConnString);

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));

app.get("/", function(req, res) {
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
    
          res.send(replyMessage.content.toString());
        })
      })
    
});

var server = app.listen(3000, function () {
    console.log("Listening on port %s...", server.address().port);
});