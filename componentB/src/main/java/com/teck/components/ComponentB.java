package com.teck.components;

import com.teck.workflow.WorkflowManagement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;

// Currently this component is coded to return (input_value*2)
public class ComponentB {
	
	private static final Logger log = LoggerFactory.getLogger(ComponentB.class);

	// Obtain the exchange name from application.properties
	@Value("${banksy.rabbitmq.exchange}")
	private String exchange;

	// This attribute specifies the queue the component will listen to
	// We obtain it from application.properties
	@RabbitListener(queues = "${banksy.rabbitmq.queue}")
	public Message processMessage(Message reqMessage) {

		// Print properties/headers of the received message
		logMessageInfo(reqMessage);

		// Touch the message to indicate server processed it
		//msgVal += " | transformer processed routing key " + routingKey;

		// Perform the actual functionality of this component: multiply
		//   the value in the body of the input message
		//TODO JTE do the actual multiplication
		String msgVal = new String(reqMessage.getBody());
		msgVal = "(" + msgVal + "*2)";

		// DEBUG: log the output message being returned
		String reqCorrId = (String)reqMessage.getMessageProperties().getHeaders().get(WorkflowManagement.X_WKF_INTERNAL_CORR_ID_HDR);
		log.info("ComponentB returning value <" + msgVal + "> for internalCorrelationId " + reqCorrId);

		// Advance the workflow to the next state
		// Internally this method sets the queue that this message will be placed
		// on upon return from this method
		Message respMessage = WorkflowManagement.advanceWorkflowStage(reqMessage, msgVal.getBytes(), exchange);

		// See note above; this message will be placed on the next queue
		// in the workflow upon return from this method
		return respMessage;
	}

	// TODO JTE move this into a shared library (also is copied in ComponentC)
	// Logs some diagnostic info about a message
	protected void logMessageInfo(Message message) {
		String reqCorrId = (String)message.getMessageProperties().getHeaders().get(WorkflowManagement.X_WKF_INTERNAL_CORR_ID_HDR);
		String replyTo = message.getMessageProperties().getReplyTo();
		String routingKey = message.getMessageProperties().getReceivedRoutingKey();
		String msgVal = new String(message.getBody());

		log.info("ComponentB Received message <" + msgVal + "> with internalCorrelationId " + reqCorrId +  " and replyTo " + replyTo + " and routingKey " + routingKey);
	}
}