package com.teck.components.api;

import com.teck.workflow.*;
import com.teck.components.RestComponentConfig;
import com.teck.components.domain.Cat;
import com.teck.components.service.CatService;

import java.io.IOException;
import java.time.LocalTime;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(value = "/v1/cats")
@RestController
public class CatController {
 
    @Autowired
    private CatService catService;

    @Autowired
    private RabbitTemplate template;
    
    @Autowired
    private RestComponentConfig config;

    private static final Logger log = LoggerFactory.getLogger(CatController.class);

    @RequestMapping(method = RequestMethod.POST,
                    produces = "application/json")
    public Cat createCat(@RequestBody Cat cat) {
        
        if(cat.getNumPaws() < 3) {
            throw new IllegalArgumentException("Not enough paws!");
        }

        String wkflresult = runWorkflow(cat);
        
        // Update the cat and return it
        cat.setLastUpdated(LocalTime.now());
        cat.setName(wkflresult);
        catService.updateCat(cat);
        return cat;
    }
     
    protected String runWorkflow(Cat cat)  {

        String respVal = "";

		// Grab a workflow descriptor
		// TODO - obtain from config server
		String WORKFLOW_DESCRIPTOR = "{ \"remainWkflw\":[ {\"Name\":\"API Controller-receive\", \"NextAddr\":\"transform.psd2toisf\"}, {\"Name\":\"Transform psd2toisf\", \"NextAddr\":\"transform.isftoiso20022\"}, {\"Name\":\"Transform isftoiso20022\", \"NextAddr\":\"transmit.tobank1\"}, {\"Name\":\"Transmit-ToBank1\", \"NextAddr\":\"transform.iso20022resptoisf\"}, {\"Name\":\"Transform-iso20022resptoisf\", \"NextAddr\":\"transform.isftopsd2resp\"}, {\"Name\":\"Transform-isftopsd2resp\", \"NextAddr\":\"Origin\"}  ]}";

		// Create payload
		String payload = cat.toString();

		// Build a message
		Message mqMessage =  MessageBuilder
			.withBody(payload.getBytes())
			.build();
			
		log.info("Sending message <" + payload + ">");

		try {
			// Begin workflow and wait for ultimate response; confirms that internal correlationId matches req-resp
			Message respMessage = (Message)WorkflowManagement.beginWorkflowAndReceive( WORKFLOW_DESCRIPTOR, mqMessage, template, config.replyQueueName());

			// DEBUG ONLY
			respVal = new String(respMessage.getBody());
            log.info("Sent <" + payload + ">, received <" + respVal +">]");	
		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}

        return respVal;
    }
        
    @ExceptionHandler
    void handleIllegalArgumentException(
                      IllegalArgumentException e,
                      HttpServletResponse response) throws IOException {
 
        response.sendError(HttpStatus.SC_BAD_REQUEST);
 
    }
}
