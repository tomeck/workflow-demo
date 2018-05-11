package com.teck.workflow;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import org.springframework.amqp.core.Address;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

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

    // When non-empty, indicates the message carries an exception
    public static final String X_WKF_ERROR_MSG_HDR = "X-WKF-ERROR-MSG"; 

    // Json attribute name within X-WKF-ROUTE-REMAIN
    public static final String REMAIN_WKFL_KEY = "remainWkflw";

    // When this value appears as the next step in a workflow, replace the next routing key
    // with the value of headers[X_WKF_TERMINAL_ADDR_HDR]
    public static final String RETURN_TO_ORIGINATOR_ADDR = "reply-to";


    // TODO refactor the two overloads of advanceWorkflowStage
    public static Address advanceWorkflowStage(Message message, String exchange) {

        Address nextAddress = null;  // return value

        // Get the remaining/processed headers from the message
        // TODO - handle if they are missing
        Map<String, Object> headers = message.getMessageProperties().getHeaders();
        String routeRemaining = (String)headers.get(X_WKF_ROUTE_REMAIN_HDR);
        String routeProcessed = (String)headers.get(X_WKF_ROUTE_PROCESSED_HDR);

        // TODO DEBUG
        System.out.println("incoming workflow remain: " + routeRemaining);
        System.out.println("incoming processed: " + routeProcessed);

        if(!routeRemaining.isEmpty() ) {

            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode routeRemainTree = objectMapper.readTree(routeRemaining);
                ArrayNode remainWkflwArray = (ArrayNode)routeRemainTree.get(REMAIN_WKFL_KEY);

                if( remainWkflwArray != null) {

                    // Current front of the remaining workflow list
                    JsonNode curStepNode = remainWkflwArray.get(0);
                    JsonNode curStepNameNode = curStepNode.get("Name");
                    String curStepNameText = curStepNameNode.asText();
                    JsonNode curStepNextAddrNode = curStepNode.get("NextAddr");
                    if( curStepNextAddrNode != null ) {
                        String nextRoutingKey = curStepNode.get("NextAddr").asText();

                        // Handle special case where NextAddr=reply-to, 
                        // in which case set the routing key to the original reply-to addr
                        if(nextRoutingKey.equals(RETURN_TO_ORIGINATOR_ADDR)) {
                            nextRoutingKey = (String)headers.get(X_WKF_TERMINAL_ADDR_HDR);
                            nextAddress = new Address(nextRoutingKey);
                        } else {
                            nextAddress = new Address(exchange, nextRoutingKey);
                        }
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

                    headers.put(X_WKF_ROUTE_REMAIN_HDR, routeRemaining);
                    headers.put(X_WKF_ROUTE_PROCESSED_HDR, routeProcessed);
                    
                    // TODO DEBUG
                    System.out.println("outgoing workflow remain: <" + routeRemaining +">");
                    System.out.println("outgoing processed: <" + routeProcessed +">");
                    System.out.println("next address: <" + nextAddress +">");
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

        return nextAddress;
    }

    //TODO figure out how to not have to pass exchange
    public static Message advanceWorkflowStage(Message reqMessage, byte[] msgBody, String exchange) {

        Message respMsg = null;  // return value

        // Get the remaining/processed headers from the message
        // TODO - handle if they are missing

        MessageProperties properties = reqMessage.getMessageProperties();
        Map<String, Object> headers = properties.getHeaders();
        String routeRemaining = (String)headers.get(X_WKF_ROUTE_REMAIN_HDR);
        String routeProcessed = (String)headers.get(X_WKF_ROUTE_PROCESSED_HDR);

        // TODO DEBUG
        System.out.println("incoming workflow remain: " + routeRemaining);
        System.out.println("incoming processed: " + routeProcessed);

        if(!routeRemaining.isEmpty() ) {

            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode routeRemainTree = objectMapper.readTree(routeRemaining);
                ArrayNode remainWkflwArray = (ArrayNode)routeRemainTree.get(REMAIN_WKFL_KEY);

                if( remainWkflwArray != null) {

                    // Current front of the remaining workflow list
                    JsonNode curStepNode = remainWkflwArray.get(0);
                    JsonNode curStepNameNode = curStepNode.get("Name");
                    String curStepNameText = curStepNameNode.asText();
                    JsonNode curStepNextAddrNode = curStepNode.get("NextAddr");
                    Address nextAddress = null;

                    if( curStepNextAddrNode != null ) {
                        String nextRoutingKey = curStepNode.get("NextAddr").asText();

                        // Handle special case where NextAddr=reply-to, 
                        // in which case set the routing key to the original reply-to addr
                        if(nextRoutingKey.equals(RETURN_TO_ORIGINATOR_ADDR)) {
                            nextRoutingKey = (String)headers.get(X_WKF_TERMINAL_ADDR_HDR);
                            nextAddress = new Address(nextRoutingKey);
                        } else {
                            nextAddress = new Address(exchange, nextRoutingKey);
                        }
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

                    headers.put(X_WKF_ROUTE_REMAIN_HDR, routeRemaining);
                    headers.put(X_WKF_ROUTE_PROCESSED_HDR, routeProcessed);
                    properties.setReplyToAddress(nextAddress);
                    
                    respMsg = MessageBuilder
                        .withBody(msgBody)
                        .copyHeaders(headers)
                        .copyProperties(properties)
                        .build();

                    // TODO DEBUG
                    System.out.println("outgoing workflow remain: <" + routeRemaining +">");
                    System.out.println("outgoing processed: <" + routeProcessed +">");
                    System.out.println("next address: <" + nextAddress +">");
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

        return respMsg;
    }

    // Send the exception to the queue specified in the X_WKF_ERROR_ADDR_HDR header
    public static Message handleError( Message offendingMessage, Exception e) {

        MessageProperties properties = offendingMessage.getMessageProperties();
        Map<String, Object> headers = properties.getHeaders();
        properties.setReplyToAddress(new Address((String)headers.get(X_WKF_ERROR_ADDR_HDR)));
        headers.put(X_WKF_ERROR_MSG_HDR, true);
        String errMessage = e.toString();

        Message respMsg = MessageBuilder
            .withBody(errMessage.getBytes())
            .copyHeaders(headers)
            .copyProperties(properties)
            .build();

        return respMsg;
    }
    
    public static Message beginWorkflowAndReceive( String workflowDescriptor, Message reqMessage, RabbitTemplate template, String replyQueueName ) throws Exception {
 
        // Add the workflow headers to the message
        addWorkflowHeaders(workflowDescriptor, replyQueueName, reqMessage);

        // Since we just processed the first stage of the workflow, pop it and advance to the next stage
        Address nextAddress = advanceWorkflowStage(reqMessage, template.getExchange());

        // Perform blocking send/receive (i.e. waiting on last step of workflow to post response to it)
        Object response = template.sendAndReceive(nextAddress.getRoutingKey(), reqMessage);

        if( response == null ) {
			throw new Exception("Client timed-out waiting for MQ response");
		}

        // Convert message
		Message respMessage = (Message)response;

        //TODO DEBUG ONLY ------------?????
		// Compare INTERNAL correlation id's
		Object rcvCorrId = respMessage.getMessageProperties().getHeaders().get(WorkflowManagement.X_WKF_INTERNAL_CORR_ID_HDR); 
        if( !rcvCorrId.equals(reqMessage.getMessageProperties().getHeaders().get(WorkflowManagement.X_WKF_INTERNAL_CORR_ID_HDR))) {
            throw new Exception("CORRELATIONIDS DO NOT MATCH!");
		}

        return respMessage;
    }

    // Populate the workflow headers on a message; called only once per workflow execution
    protected static void addWorkflowHeaders(String workflowDescriptor, String replyQueueName, Message message) {

        // The remaining route==the full route
        String X_WKF_ROUTE_REMAIN = workflowDescriptor;

        // The route processed thus far==null
        String X_WKF_ROUTE_PROCESSED = "";

        // Generate debug correlation Id and set on message
		String X_WKF_INTERNAL_CORR_ID = UUID.randomUUID().toString();
        
        message.getMessageProperties().getHeaders().put(X_WKF_INTERNAL_CORR_ID_HDR, X_WKF_INTERNAL_CORR_ID);
        message.getMessageProperties().getHeaders().put(X_WKF_ROUTE_REMAIN_HDR, X_WKF_ROUTE_REMAIN);
        message.getMessageProperties().getHeaders().put(X_WKF_ROUTE_PROCESSED_HDR, X_WKF_ROUTE_PROCESSED);
        // The address to which a message shall be routed upon successful completion of workflow
        message.getMessageProperties().getHeaders().put(WorkflowManagement.X_WKF_TERMINAL_ADDR_HDR, replyQueueName);
        // The address to which a message shall be routed upon error/exception
        message.getMessageProperties().getHeaders().put(WorkflowManagement.X_WKF_ERROR_ADDR_HDR, replyQueueName);    
        
    }
}