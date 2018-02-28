var doWorkflow = require('../amqplib-rpc').doWorkflow
var amqplib = require('amqplib')

var amqpConnString = 'amqp://guest:guest@localhost:5672'
var open = amqplib.connect(amqpConnString);

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

open.then(function(connection) {
    // var content = 'Hello from Node'
    // var workflow = {remainWkflw:[ {Name:'B', NextAddr:'processed'}, {Name:'C', NextAddr:'requests'}, {Name:'D', NextAddr:'processed'}, {Name:'Z', NextAddr:'reply-to'}]}
    // var opts = { exchangeName : 'wf-demo'}

    var content = initiatePaymentPayload
    var workflow = { remainWkflw:[ {Name:"psd2-uk-to-isf", NextAddr:"banksy.q2"}, {Name:"internal-router1", NextAddr:"banksy.q3"}, {Name:"isf-to-pacs008", NextAddr:"banksy.q4"}, {Name:"internal-router3", NextAddr:"banksy.q5"}, {Name:"internal-router4", NextAddr:"reply-to"} ]}
    var opts = { exchangeName : 'banksy', headers : { bankID : "000001-CMA_Bank_1-GBR"}}
    
    // RPC request: sends to routing-key (2nd param) of exchange specified in opts (above)
    // Specifies the workflow to execute
    // Waits for the workflow to complete (i.e. the last step must have NextAddr:reply-to)
    doWorkflow(connection, 'banksy.q1', JSON.stringify(content), JSON.stringify(workflow), opts, function (err, replyMessage) {
      if (err) throw err
      console.log('***NODE SERVER RECEIVED: '+replyMessage.content.toString())

      process.exit()
    })
  })

