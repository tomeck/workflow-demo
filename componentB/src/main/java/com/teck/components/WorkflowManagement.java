package com.teck.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.Map;
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

    // Json attribute name within X-WKF-ROUTE-REMAIN
    public static final String REMAIN_WKFL_KEY = "remainWkflw";

    public static String advanceWorkflowStage(Message message) {

        String nextRoutingKey = null;  // return value

        // Get the remaining/processed headers from the message
        // TODO - handle if they are missing
        Map<String, Object> headers = message.getMessageProperties().getHeaders();
        String routeRemaining = (String)headers.get(X_WKF_ROUTE_REMAIN_HDR);
        String routeProcessed = (String)headers.get(X_WKF_ROUTE_PROCESSED_HDR);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode routeRemainTree = objectMapper.readTree(routeRemaining);
            ArrayNode remainWkflwArray = (ArrayNode)routeRemainTree.get(REMAIN_WKFL_KEY);

            // TODO DEBUG
            System.out.println("incoming workflow remain: " + remainWkflwArray);
            System.out.println("incoming processed: " + routeProcessed);

            if( remainWkflwArray != null) {

                // Current front of the remaining workflow list
                JsonNode curStepNode = remainWkflwArray.get(0);
                JsonNode curStepNameNode = curStepNode.get("Name");
                String curStepNameText = curStepNameNode.asText();
                JsonNode curStepNextAddrNode = curStepNode.get("NextAddr");
                if( curStepNextAddrNode != null ) {
                    nextRoutingKey = curStepNode.get("NextAddr").asText();
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
                System.out.println("next routingKey: <" + nextRoutingKey +">");
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

        return nextRoutingKey;
    }

    public static Message beginWorkflowAndReceive( String workflowDescriptor, Message reqMessage, RabbitTemplate template ) throws Exception {
 
        // Add the workflow headers to the message
        addWorkflowHeaders(workflowDescriptor, reqMessage, template);

        // Since we just processed the first stage of the workflow, pop it and advance to the next stage
        String nextRoutingKey = advanceWorkflowStage(reqMessage);

        // Perform blocking send/receive (i.e. waiting on last step of workflow to post response to it)
        Object response = template.sendAndReceive(nextRoutingKey, reqMessage);

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
    protected static void addWorkflowHeaders(String workflowDescriptor, Message message, RabbitTemplate template) {

        //TODO the reply-to addresses are currently set in the Config Classes' postProcessMessage()

        // The remaining route==the full route
        String X_WKF_ROUTE_REMAIN = workflowDescriptor;

        // The route processed thus far==null
        String X_WKF_ROUTE_PROCESSED = "";

        // Generate debug correlation Id and set on message
		String X_WKF_INTERNAL_CORR_ID = UUID.randomUUID().toString();
        
        message.getMessageProperties().getHeaders().put(X_WKF_INTERNAL_CORR_ID_HDR, X_WKF_INTERNAL_CORR_ID);
        message.getMessageProperties().getHeaders().put(X_WKF_ROUTE_REMAIN_HDR, X_WKF_ROUTE_REMAIN);
        message.getMessageProperties().getHeaders().put(X_WKF_ROUTE_PROCESSED_HDR, X_WKF_ROUTE_PROCESSED);
    }
}