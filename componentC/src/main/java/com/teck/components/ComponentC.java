package com.teck.components;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Address;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

public class ComponentC {

	@Autowired
	private RabbitTemplate template;

	private static final Logger log = LoggerFactory.getLogger(ComponentC.class);
	
	@RabbitListener(queues = ComponentCConfig.INCOMING_QUEUENAME)
	//@RabbitListener(queues = "tut.rpc.requests")
	//@SendTo("foobar") //used when the client doesn't set replyTo.
	public Message processMessage(Message reqMessage) {

		Message respMessage = null;

		// Print properties/headers of the received message
		//String correlationId = reqMessage.getMessageProperties().getCorrelationIdString();
		String reqCorrId = (String)reqMessage.getMessageProperties().getHeaders().get(WorkflowManagement.X_WKF_INTERNAL_CORR_ID_HDR);
		String replyTo = reqMessage.getMessageProperties().getReplyTo();
		//String eckCorrId = (String)reqMessage.getMessageProperties().getHeaders().get("ECK_CORR_ID");
		String msgVal = new String(reqMessage.getBody());

		log.info("Received message <" + msgVal + "> with internalCorrelationId " + reqCorrId +  " and replyTo " + replyTo );

		// Touch the message to indicate server processed it
		msgVal += " | Hello from ComponentC";

		/*
		Message respMessage = MessageBuilder
			.withBody(msgVal.getBytes())
			.copyHeaders(reqMessage.getMessageProperties().getHeaders())
			.copyProperties(reqMessage.getMessageProperties())
			.build();
		*/

		//String nextRoutingKey = WorkflowManagement.advanceWorkflowStage(reqMessage);
		//TODO figure out how to not have to pass template.getExchange()
		Address newAddr = WorkflowManagement.advanceWorkflowStage(reqMessage, template.getExchange());
				
		if( newAddr != null ) {
			// TODO do this if routingKey == reply-to
			MessageProperties reqProps = reqMessage.getMessageProperties();
			Map<String, Object> reqHeaders = reqProps.getHeaders();

			// Get original replyTo address from header
			//String sourceReplyTo = (String)reqHeaders.get(WorkflowManagement.X_WKF_TERMINAL_ADDR_HDR);

			// Route to new next queue
			//Address newAddr = new Address(sourceReplyTo);
			//Address newAddr = new Address(template.getExchange(), nextRoutingKey);
			reqProps.setReplyToAddress(newAddr);

			respMessage = MessageBuilder
				.withBody(msgVal.getBytes())
				.copyHeaders(reqHeaders)
				.copyProperties(reqProps)
				.build();
		}

		return respMessage;
	}
}