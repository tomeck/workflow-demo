package com.teck.components;

import com.teck.workflow.*;

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

public class ComponentB {
	
	@Autowired
	private RabbitTemplate template;

	private static final Logger log = LoggerFactory.getLogger(ComponentB.class);
	
	@RabbitListener(queues = ComponentBConfig.INCOMING_QUEUENAME)
	public Message processMessage(Message reqMessage) {

		// TODO DEBUG ONLY
		// Print properties/headers of the received message
		String reqCorrId = (String)reqMessage.getMessageProperties().getHeaders().get(WorkflowManagement.X_WKF_INTERNAL_CORR_ID_HDR);
		String replyTo = reqMessage.getMessageProperties().getReplyTo();
		String msgVal = new String(reqMessage.getBody());

		log.info("Received message <" + msgVal + "> with internalCorrelationId " + reqCorrId +  " and replyTo " + replyTo );

		// Touch the message to indicate server processed it
		msgVal += " | Hello from ComponentB";

		Message respMessage = WorkflowManagement.advanceWorkflowStage(reqMessage, msgVal.getBytes(), template.getExchange());

		return respMessage;
	}
}