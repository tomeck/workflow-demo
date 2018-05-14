package com.teck.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;

public class WorkflowPersisterComponent {
	
	private static final Logger log = LoggerFactory.getLogger(WorkflowPersisterComponent.class);

	// Read exchange name from application.properties
	@Value("${banksy.rabbitmq.exchange}")
	private String exchange;

	@RabbitListener(queues = "${banksy.rabbitmq.queue}")
	public Message processMessage(Message reqMessage) {

		// TODO DEBUG ONLY
		// Print properties/headers of the received message
		// TODO - do i need to pull in Workflow-lib?
		//String reqCorrId = (String)reqMessage.getMessageProperties().getHeaders().get(WorkflowManagement.X_WKF_INTERNAL_CORR_ID_HDR);
		String reqCorrId = "???";
		String replyTo = reqMessage.getMessageProperties().getReplyTo();
		String routingKey = reqMessage.getMessageProperties().getReceivedRoutingKey();
		String msgVal = new String(reqMessage.getBody());

		log.info("WorkflowPersisterComponent Received message <" + msgVal + "> with internalCorrelationId " + reqCorrId +  " and replyTo " + replyTo + " and routingKey " + routingKey);

		return null;
	}
}