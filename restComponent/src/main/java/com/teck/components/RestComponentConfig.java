package com.teck.components;

import java.util.UUID;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RestComponentConfig {

    // Read config values from application.properties
	@Value("${banksy.rabbitmq.exchange}")
	private String exchange;

    @Value("${banksy.rabbitmq.reply-queue-base}")
	private String replyQueuenameBase;

    @Value("${banksy.rabbitmq.reply-routing-key}")
	private String replyRoutingKey;

    @Value("${banksy.rabbitmq.reply-timeout}")
	private long replyTimeout;


    private String uniqueReplyQueueName;

    @Autowired
    private ConnectionFactory connectionFactory;

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(exchange);
    }

    public String replyQueueName() {
        return uniqueReplyQueueName;
    }

    @Bean
    public Queue replyQueue() {
        if( uniqueReplyQueueName == null) {
            uniqueReplyQueueName = replyQueuenameBase + UUID.randomUUID().toString();
            System.out.println("ComponentA unique reply queue name is " + uniqueReplyQueueName);
        }
        return new Queue(uniqueReplyQueueName, false, true, true);
    }

    @Bean
    public RabbitTemplate amqpTemplate() {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setExchange(exchange().getName());
        rabbitTemplate.setRoutingKey(replyRoutingKey);
        rabbitTemplate.setReplyAddress(uniqueReplyQueueName);
        rabbitTemplate.setReplyTimeout(replyTimeout);
        rabbitTemplate.setReplyQueue(replyQueue());
        //rabbitTemplate.setBeforePublishPostProcessors(this); // causes this.postProcessMessage() to be invoked after message has been created
        return rabbitTemplate;
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
}
