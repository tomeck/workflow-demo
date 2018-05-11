package com.teck.components;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;

/*
import org.slf4j.Logger;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.amqp.rabbit.listener.exception.ListenerExecutionFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ErrorHandler;
*/

@Configuration
@EnableRabbit
public class ComponentCConfig {

    // TODO JTE uncomment code below if want to customize RabbitListener, e.g. to install custom error handler
    //
    // TODO JTE factor all the code in ComponentBConfig into protected base class ComponentConfig
    /*
    @Autowired
    private ConnectionFactory connectionFactory;
    
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory() {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setConcurrentConsumers(5);  // TODO JTE this should be read from application.properties
        factory.setMaxConcurrentConsumers(20); // TODO ditto
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
    */

    @Bean
    public ComponentC server() {
        return new ComponentC();
    }
}