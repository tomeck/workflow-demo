package com.teck.components;

import com.teck.workflow.*;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

public class ComponentA implements CommandLineRunner {

	@Autowired
	private RabbitTemplate template;

	@Autowired
    private ComponentAConfig config;

	private static final Logger log = LoggerFactory.getLogger(ComponentA.class);

	@Override
    public void run(String... args)  {

		//jsonFun();

		int numLoops = 1000; // will run out of thread space if increase too much more

		for (int i = 0; i < numLoops; i++) {
			//startWorkflow();
			// JTE make all the workflows run in parallel
			new Thread( () -> startWorkflow() ).start();
		}
	}
	
	protected void jsonFun() {

		final String REMAIN_WKFL_KEY = "remainWkflw";

        String routeRemaining = "{ \"remainWkflw\":[ {\"Name\":\"A\", \"NextAddr\":\"requests\"}, {\"Name\":\"B\", \"NextAddr\":\"processed\"}, {\"Name\":\"C\", \"NextAddr\":\"reply-to\"}  ]}";
        String routeProcessed = "";
		String nextRoutingKey = "";

		while( routeRemaining.length() > 0 ) {
			ObjectMapper objectMapper = new ObjectMapper();
			try {
				// Pop the front of the remaining route list
				JsonNode routeRemainTree = objectMapper.readTree(routeRemaining);
				//JsonNode remainWkflwNode = routeRemainTree.get("remainWkflw");
				//List<JsonNode> wkflList = objectMapper.readValue("remainWkflw", new TypeReference<List<String>>(){});
				ArrayNode remainWkflwArray = (ArrayNode)routeRemainTree.get(REMAIN_WKFL_KEY);


				System.out.println("incoming workflow remain: " + remainWkflwArray);
				System.out.println("incoming processed: " + routeProcessed);

				if( remainWkflwArray != null) {

					// Current front of the remaining workflow list
					JsonNode curStepNode = remainWkflwArray.get(0);
					JsonNode curStepNameNode = curStepNode.get("Name");
					String curStepNameText = curStepNameNode.asText();
					JsonNode curStepNextAddrNode = curStepNode.get("NextAddr");
					if( curStepNextAddrNode != null ) {
						nextRoutingKey = curStepNode.get("NextAddr").asText();
					}

					// Pop workflow
					ArrayNode updatedWorkflowArray = objectMapper.createArrayNode();
					for (int i = 1; i < remainWkflwArray.size(); i++) {
						updatedWorkflowArray.add(remainWkflwArray.get(i));
					}

					// Update processed list
					routeProcessed += (routeProcessed.isEmpty() ? "" : "|") + curStepNameText;

					// Update message headers
					if(updatedWorkflowArray.size()>0) {
						ObjectNode newWflwNode = objectMapper.createObjectNode();
						newWflwNode.set(REMAIN_WKFL_KEY, updatedWorkflowArray);
						routeRemaining = objectMapper.writeValueAsString(newWflwNode);
					}
					else {
						routeRemaining = "";
					}

					System.out.println("outgoing workflow remain: <" + routeRemaining +">");
					System.out.println("outgoing processed: <" + routeProcessed +">");
					System.out.println("next routingKey: <" + nextRoutingKey +">");
				}
				else {
					// TODO WHAT TO DO??
				}
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	protected void startWorkflow()  {

		// Grab a workflow descriptor
		// TODO - obtain from config server
        //String WORKFLOW_DESCRIPTOR = "{ \"remainWkflw\":[ {\"Name\":\"componentA\", \"NextAddr\":\"banksy.q1\"}, {\"Name\":\"psd2-uk-to-isf\", \"NextAddr\":\"banksy.q2\"},{\"Name\":\"internal-router\", \"NextAddr\":\"banksy.q3\"}, {\"Name\":\"internal-router\", \"NextAddr\":\"reply-to\"} ]}";
        //String WORKFLOW_DESCRIPTOR = "{ \"remainWkflw\":[ {\"Name\":\"A\", \"NextAddr\":\"requests\"}, {\"Name\":\"B\", \"NextAddr\":\"processed\"}, {\"Name\":\"C\", \"NextAddr\":\"reply-to\"}  ]}";
        String WORKFLOW_DESCRIPTOR = "{ \"remainWkflw\":[ {\"Name\":\"apicontroller\", \"NextAddr\":\"transform.psd2toisf\"}, {\"Name\":\"Transformer-1-transform.psd2toisf\", \"NextAddr\":\"transform.isftoiso20022\"}, {\"Name\":\"Transformer-2-isftoiso20022\", \"NextAddr\":\"transmit.tobank1\"}, {\"Name\":\"Transmitter-1-tobank1\", \"NextAddr\":\"transform.iso20022resptoisf\"}, {\"Name\":\"Transformer-3-iso20022resptoisf\", \"NextAddr\":\"transform.isftopsd2resp\"}, {\"Name\":\"Transformer-4-isftopsd2resp\", \"NextAddr\":\"reply-to\"}  ]}";

		// Create payload
		String payload = "Hello from Component A";

		// Build a message
		Message mqMessage =  MessageBuilder
			.withBody(payload.getBytes())
			.build();
			
		log.info("Sending message <" + payload + ">");

		try {
			// Begin workflow and wait for ultimate response; confirms that internal correlationId matches req-resp
			Message respMessage = (Message)WorkflowManagement.beginWorkflowAndReceive( WORKFLOW_DESCRIPTOR, mqMessage, template, config.replyQueueName());

			// DEBUG ONLY
			String respVal = new String(respMessage.getBody());
			log.info("Sent <" + payload + ">, received <" + respVal +">]");	
		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}
	}
}