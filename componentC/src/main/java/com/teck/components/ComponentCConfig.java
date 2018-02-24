package com.teck.components;


import org.slf4j.Logger;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.amqp.rabbit.listener.exception.ListenerExecutionFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ErrorHandler;

@Configuration
@EnableRabbit
public class ComponentCConfig {

    public static final String INCOMING_QUEUENAME = "tut.rpc.processed"; // was "tut.rpc.requests"
    private final String EXCHANGE_NAME = "tut.rpc";
    private final String REQUEST_ROUTING_KEY = "processed";

    @Autowired
    private ConnectionFactory connectionFactory;
    
    @Bean
    public Queue incomingQueue() {
        return new Queue(INCOMING_QUEUENAME);
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Binding binding(DirectExchange exchange, Queue queue) {
        return BindingBuilder.bind(queue).to(exchange).with(REQUEST_ROUTING_KEY);
    }

    @Bean
    public RabbitTemplate amqpTemplate() {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setExchange(exchange().getName());
        return rabbitTemplate;
    }
    
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory() {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setConcurrentConsumers(5);
        factory.setMaxConcurrentConsumers(20);
        factory.setErrorHandler(errorHandler());
        return factory;
    }

    @Bean
	public ErrorHandler errorHandler() {
		return new ConditionalRejectingErrorHandler(new MyFatalExceptionStrategy());
	}

    public static class MyFatalExceptionStrategy extends ConditionalRejectingErrorHandler.DefaultExceptionStrategy {

		private final Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());

		@Override
		public boolean isFatal(Throwable t) {
			if (t instanceof ListenerExecutionFailedException) {
                ListenerExecutionFailedException lefe = (ListenerExecutionFailedException) t;
                String errorMsg = "Failed to process inbound message from queue "
                    + lefe.getFailedMessage().getMessageProperties().getConsumerQueue()
                    + "; failed message: " + lefe.getFailedMessage();
				logger.error(errorMsg, t);
                        
                // JTE I'm going to consider this fatal, so don't requeue
                throw new AmqpRejectAndDontRequeueException(errorMsg);
			}
			return super.isFatal(t);
		}

	}
    @Bean
    public ComponentC server() {
        return new ComponentC();
    }
}