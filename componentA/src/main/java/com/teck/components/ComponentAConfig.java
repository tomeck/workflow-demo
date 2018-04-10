package com.teck.components;

import com.teck.workflow.*;

import java.util.Map;
import java.util.UUID;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ComponentAConfig implements MessagePostProcessor {

    private static final String EXCHANGE_NAME =  "wf-demo";
    private static final String REPLY_QUEUE_NAME_BASE = "wf-demo.replies.";
    private static final String REPLY_ROUTING_KEY = "reply";
    private static final long REPLY_TIMEOUT = 200000;

    private String uniqueReplyQueueName;

    @Autowired
    private ConnectionFactory connectionFactory;

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue replyQueue() {
        if( uniqueReplyQueueName == null) {
            uniqueReplyQueueName = REPLY_QUEUE_NAME_BASE + UUID.randomUUID().toString();
            System.out.println("mqclient unique reply queue name is " + uniqueReplyQueueName);
        }
        return new Queue(uniqueReplyQueueName, false, true, true);
    }

    @Bean
    public RabbitTemplate amqpTemplate() {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setExchange(exchange().getName());
        rabbitTemplate.setRoutingKey(REPLY_ROUTING_KEY);
        //rabbitTemplate.setReplyAddress(EXCHANGE_NAME + "/" + REPLY_ROUTING_KEY);
        rabbitTemplate.setReplyAddress(uniqueReplyQueueName);
        rabbitTemplate.setReplyTimeout(REPLY_TIMEOUT);
        rabbitTemplate.setReplyQueue(replyQueue());
        //rabbitTemplate.setCorrelationKey("ECK_CORR_ID");
        rabbitTemplate.setBeforePublishPostProcessors(this); // causes this.postProcessMessage() to be invoked after message has been created
        return rabbitTemplate;
    }

        // Set header for name of queue to route message upon successful completion (this will be the unique send-to queue for each workflow instance)
        // Set header for name of error queue to route errors to
        //TODO all the other workflow headers are currently set in the WorkflowManagement.addWorkflowHeaders, so really need a way to obviate the need for this
        // But it's only needed for components that initiate a workflow
        public Message postProcessMessage(Message message) {

        Map<String, Object> headers = message.getMessageProperties().getHeaders();

        // Add the reply and error addresses to the workflow headers if not present
        if(!headers.containsKey(WorkflowManagement.X_WKF_TERMINAL_ADDR_HDR)) {

            String replyAddr = uniqueReplyQueueName;

            // The address to which a message shall be routed upon successful completion of workflow
            message.getMessageProperties().getHeaders().put(WorkflowManagement.X_WKF_TERMINAL_ADDR_HDR, replyAddr );

            // The address to which a message shall be routed upon error/exception
            message.getMessageProperties().getHeaders().put(WorkflowManagement.X_WKF_ERROR_ADDR_HDR, replyAddr);    
        }
        


        return message;
    }

    // Listener Container 
    @Bean
    public SimpleMessageListenerContainer replyListenerContainer() {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueues(replyQueue());
        container.setMessageListener(amqpTemplate());
        return container;
    }

    // Listener Container Factory
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory() {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setReceiveTimeout(10000L);
        return factory;
    }

    @Bean
    public ComponentA client() {
        return new ComponentA();
    }
}
