package com.teck.workflow;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class WorkflowManagement {


    public static Message beginWorkflowAndReceive( String workflowDescriptor, Message message, RabbitTemplate template ) {
 
        // TODO implement
        return message;
        
    }




}