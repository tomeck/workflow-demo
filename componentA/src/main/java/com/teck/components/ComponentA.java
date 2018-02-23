package com.teck.components;

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

	//TODO Remove this when find better way to get original reply-to
	@Autowired
	private ComponentAConfig config;

	private static final Logger log = LoggerFactory.getLogger(ComponentA.class);

	@Override
    public void run(String... args)  {
		int numLoops = 10000;

		for (int i = 0; i < numLoops; i++) {
			startWorkflow();
		}
	}
	
	protected void startWorkflow()  {

		// Grab a workflow descriptor
		// TODO - obtain from config server
		String WORKFLOW_DESCRIPTOR = "{[ {\'Name\':\'A\', \'NextAddr\':\'toB\'}, {\'Name\':\'B\', \'NextAddr\':\'toC\'}, {\'Name\':\'C\'}  ]}";

		// Create payload
		String payload = "Hello from Component A";

		// Build a message
		Message mqMessage =  MessageBuilder
			.withBody(payload.getBytes())
			.build();
			
		log.info("Sending message <" + payload + ">");

		try {
			// TODO - fix how the reply-to gets obtained
			String replyAddr = config.replyQueue().getName();
			Message respMessage = (Message)WorkflowManagement.beginWorkflowAndReceive( WORKFLOW_DESCRIPTOR, replyAddr, mqMessage, template);
			String respVal = new String(respMessage.getBody());

			log.info("Sent <" + payload + ">, received <" + respVal +">]");	
		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}
	}
}