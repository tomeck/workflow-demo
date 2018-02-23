package com.teck.components;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Address;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.SendTo;

public class ComponentB {
	
	private static final Logger log = LoggerFactory.getLogger(ComponentB.class);
	
	@RabbitListener(queues = "tut.rpc.requests")
	//@SendTo("foobar") //used when the client doesn't set replyTo.
	public Message processMessage(Message reqMessage) {

		// Print properties/headers of the received message
		//String correlationId = reqMessage.getMessageProperties().getCorrelationIdString();
		String reqCorrId = (String)reqMessage.getMessageProperties().getHeaders().get(WorkflowManagement.X_WKF_INTERNAL_CORR_ID_HDR);
		String replyTo = reqMessage.getMessageProperties().getReplyTo();
		//String eckCorrId = (String)reqMessage.getMessageProperties().getHeaders().get("ECK_CORR_ID");
		String msgVal = new String(reqMessage.getBody());

		log.info("Received message <" + msgVal + "> with internalCorrelationId " + reqCorrId +  " and replyTo " + replyTo );

		// Touch the message to indicate server processed it
		msgVal += " | Hello from ComponentB";
				
		MessageProperties reqProps = reqMessage.getMessageProperties();
		Map<String, Object> reqHeaders = reqProps.getHeaders();

		// Get original replyTo address from header
		String sourceReplyTo = (String)reqHeaders.get(WorkflowManagement.X_WKF_TERMINAL_ADDR_HDR);

		// Route to new next queue
		Address newAddr = new Address(sourceReplyTo);
		reqProps.setReplyToAddress(newAddr);

		Message respMessage = MessageBuilder
			.withBody(msgVal.getBytes())
			.copyHeaders(reqHeaders)
			.copyProperties(reqProps)
			.build();

		return respMessage;
	}
}