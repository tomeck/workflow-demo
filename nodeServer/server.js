var express = require("express");
var bodyParser = require("body-parser");
var app = express();

var doWorkflow = require('../amqplib-rpc').doWorkflow
var amqplib = require('amqplib')

var amqpConnString = 'amqp://guest:guest@localhost:5672'
var open = amqplib.connect(amqpConnString);

app.use(bodyParser.json());

var initiatePaymentPayload = {
    "Data": {
        "Initiation": {
            "InstructionIdentification": "5791997839278080",
            "EndToEndIdentification": "8125371765489664",
            "InstructedAmount": {
                "Amount": "700.00",
                "Currency": "EUR"
            },
            "DebtorAgent": {
                "SchemeName": "BICFI",
                "Identification": "AAAAGB2L"
            },
            "DebtorAccount": {
                "SchemeName": "IBAN",
                "Identification": "IE29AIBK93115212345678",
                "Name": "Gary Kean",
                "SecondaryIdentification": "6686302651023360"
            },
            "CreditorAgent": {
                "SchemeName": "BICFI",
                "Identification": "AAAAGB2K"
            },
            "CreditorAccount": {
                "SchemeName": "IBAN",
                "Identification": "IE29AIBK93115212345676",
                "Name": "Carlo Marcoli",
                "SecondaryIdentification": "8380390651723776"
            },
            "RemittanceInformation": {
                "Unstructured": "emeherpakkaodafeofiu",
                "Reference": "ehoorepre"
            }
        }
    },
    "Risk": {
        "PaymentContextCode": "PersonToPerson",
        "MerchantCategoryCode": "nis",
        "MerchantCustomerIdentification": "1130294929260544",
        "DeliveryAddress": {
            "AddressLine": [
                "totbelsanagrusa"
            ],
            "StreetName": "Morning Road",
            "BuildingNumber": "62",
            "PostCode": "G3 5HY",
            "TownName": "Glasgow",
            "CountrySubDivision": [
                "Scotland"
            ],
            "Country": "GB"
        }
    }
}

var workflow = { remainWkflw:[ {Name:"psd2-uk-to-isf", NextAddr:"banksy.q2"}, {Name:"internal-router1", NextAddr:"banksy.q3"}, {Name:"internal-router2", NextAddr:"banksy.q4"}, {Name:"internal-router3", NextAddr:"banksy.q5"}, {Name:"internal-router4", NextAddr:"reply-to"} ]}

//open-banking/payments
app.post("/open-banking/payments", function(req, res) {
    open.then(function(connection) {
        //console.log(req.headers)

        // var workflow = {remainWkflw:[ {Name:'B', NextAddr:'processed'}, {Name:'C', NextAddr:'requests'}, {Name:'D', NextAddr:'processed'}, {Name:'Z', NextAddr:'reply-to'}]}
        // var opts = { exchangeName : 'wf-demo'}
    
        var content = req.body  // see var initiatePaymentPayload above
        var bankID = req.headers['bankid'] //"000001-CMA_Bank_1-GBR"
        //console.log("bankid: " + bankID)
        //var content = initiatePaymentPayload

        // TODO need to fix doWorkflow() s.t. correctly merges the headers supplied in opts here
        //   with the ones generated to initially populate the workflow headers
        var opts = { exchangeName : 'banksy', headers : { bankID : bankID}}
        
        // RPC request: sends to routing-key (2nd param) of exchange specified in opts (above)
        // Specifies the workflow to execute
        // Waits for the workflow to complete (i.e. the last step must have NextAddr:reply-to)
        // TODO: the first step should be in the workflow (and in this case route to NextAddr=banksy.q1)
        doWorkflow(connection, 'banksy.q1', content, JSON.stringify(workflow), opts, function (err, replyMessage) {
          if (err) throw err
          console.log('***NODE SERVER RECEIVED: '+replyMessage.content.toString())
          res.send(replyMessage.content.toString())
        })
      })
});

var server = app.listen(3000, function () {
    console.log("Listening on port %s...", server.address().port);
});