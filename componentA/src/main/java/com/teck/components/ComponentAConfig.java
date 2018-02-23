package com.teck.components;

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

    private final String EXCHANGE_NAME = "tut.rpc";
    private final String REPLY_QUEUE_NAME_BASE = "tut.rpc.replies.";
    private final String REPLY_ROUTING_KEY = "reply";
    private final long REPLY_TIMEOUT = 200000;

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
        rabbitTemplate.setBeforePublishPostProcessors(this);
        return rabbitTemplate;
    }

    public Message postProcessMessage(Message message) {

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
