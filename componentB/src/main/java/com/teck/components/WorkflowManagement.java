package com.teck.components;

import java.util.UUID;

// TODO MOVE THIS TO WORKFLOW-LIB

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class WorkflowManagement {

    // Internal correlation id used only for debugging (NOTE: Spring/Rabbit does not look at this header)
    public static final String X_WKF_INTERNAL_CORR_ID_HDR = "X-WKF-INTERNAL-CORR-ID";

    // The address to which a message shall be routed upon successful completion of workflow
    public static final String X_WKF_TERMINAL_ADDR_HDR = "X-WKF-TERMINAL-ADDR";

    // The address to which a message shall be routed upon error/exception
    public static final String X_WKF_ERROR_ADDR_HDR = "X-WKF-ERROR-ADDR";

    // The remaining route
    public static final String X_WKF_ROUTE_REMAIN_HDR = "X-WKF-ROUTE-REMAIN";

    // The route processed thus far
    public static final String X_WKF_ROUTE_PROCESSED_HDR = "X-WKF-ROUTE-PROCESSED"; 

    // TODO get rid of replyAddr when figure out how to get original reply-to address
    public static Message beginWorkflowAndReceive( String workflowDescriptor, String replyAddr, Message reqMessage, RabbitTemplate template ) throws Exception {
 
        // Add the workflow headers to the message
        // TODO get rid of replyAddr when figure out how to get original reply-to address
        addWorkflowHeaders(workflowDescriptor, reqMessage, template, replyAddr);

        // TODO pop workflowDescriptor to get next routingKey, etc

        String routingKey = "requests";

        // Perform blocking send/receive (i.e. waiting on last step of workflow to post response to it)
        Object response = template.sendAndReceive(routingKey, reqMessage);

        if( response == null ) {
			throw new Exception("Client timed-out waiting for MQ response");
		}

        // Convert message
		Message respMessage = (Message)response;

        //TODO DEBUG ONLY ------------?????
		// Compare INTERNAL correlation id's
		Object rcvCorrId = respMessage.getMessageProperties().getHeaders().get(WorkflowManagement.X_WKF_INTERNAL_CORR_ID_HDR);    //new String(respMessage.getMessageProperties().getCorrelationId());
		//if( !rcvCorrId.equals(message.getMessageProperties().getCorrelationId())) {
        if( !rcvCorrId.equals(reqMessage.getMessageProperties().getHeaders().get(WorkflowManagement.X_WKF_INTERNAL_CORR_ID_HDR))) {
            throw new Exception("CORRELATIONIDS DO NOT MATCH!");
		}


        return respMessage;
    }

    // Populate the workflow headers on a message; called only once per workflow execution
    // TODO get rid of replyAddr when figure out how to get original reply-to address
    protected static void addWorkflowHeaders(String workflowDescriptor, Message message, RabbitTemplate template, String replyAddr) {

        // The address to which a message shall be routed upon successful completion of workflow
        String X_WKF_TERMINAL_ADDR = replyAddr; //message.getMessageProperties().getReplyTo();

        // The address to which a message shall be routed upon error/exception
        String X_WKF_ERROR_ADDR = replyAddr; //message.getMessageProperties().getReplyTo();

        // The remaining route==the full route
        String X_WKF_ROUTE_REMAIN = workflowDescriptor;

        // The route processed thus far==null
        String X_WKF_ROUTE_PROCESSED = "";

        // Generate debug correlation Id and set on message
		String X_WKF_INTERNAL_CORR_ID = UUID.randomUUID().toString();
        
        message.getMessageProperties().getHeaders().put(X_WKF_INTERNAL_CORR_ID_HDR, X_WKF_INTERNAL_CORR_ID);
        message.getMessageProperties().getHeaders().put(X_WKF_TERMINAL_ADDR_HDR, X_WKF_TERMINAL_ADDR);
        message.getMessageProperties().getHeaders().put(X_WKF_ERROR_ADDR_HDR, X_WKF_ERROR_ADDR);
        message.getMessageProperties().getHeaders().put(X_WKF_ROUTE_REMAIN_HDR, X_WKF_ROUTE_REMAIN);
        message.getMessageProperties().getHeaders().put(X_WKF_ROUTE_PROCESSED_HDR, X_WKF_ROUTE_PROCESSED);
    }



}