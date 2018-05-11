package com.teck.components;

import com.teck.workflow.WorkflowManagement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;

public class ComponentC {
	
	private static final Logger log = LoggerFactory.getLogger(ComponentC.class);

	// Read exchange name from application.properties
	@Value("${banksy.rabbitmq.exchange}")
	private String exchange;

	@RabbitListener(queues = "${banksy.rabbitmq.queue}")
	public Message processMessage(Message reqMessage) {

		// TODO DEBUG ONLY
		// Print properties/headers of the received message
		String reqCorrId = (String)reqMessage.getMessageProperties().getHeaders().get(WorkflowManagement.X_WKF_INTERNAL_CORR_ID_HDR);
		String replyTo = reqMessage.getMessageProperties().getReplyTo();
		String routingKey = reqMessage.getMessageProperties().getReceivedRoutingKey();
		String msgVal = new String(reqMessage.getBody());

		log.info("TRANSMITTER Received message <" + msgVal + "> with internalCorrelationId " + reqCorrId +  " and replyTo " + replyTo + " and routingKey " + routingKey);

		// Touch the message to indicate server processed it
		msgVal += " | transmitter processed routing key " + routingKey;

		Message respMessage = WorkflowManagement.advanceWorkflowStage(reqMessage, msgVal.getBytes(), exchange);

		return respMessage;
	}
}