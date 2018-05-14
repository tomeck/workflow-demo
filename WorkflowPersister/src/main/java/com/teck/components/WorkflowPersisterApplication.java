package com.teck.components;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
/*import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.context.ConfigurableApplicationContext;*/

@SpringBootApplication
@EnableAutoConfiguration
public class WorkflowPersisterApplication {

	public static void main(String[] args) {
		SpringApplication.run(WorkflowPersisterApplication.class, args);

		/*
		ConfigurableApplicationContext ctx = SpringApplication.run(WorkflowPersisterApplication.class, args);

		ConnectionFactory connectionFactory = ctx.getBean(ConnectionFactory.class);
		RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
		AbstractMessageListenerContainer container = startListening(rabbitAdmin, rabbitAdmin.declareQueue(),
				new TopicExchange("wf-demo-topic"), "#", message -> {
					System.out.println(new String(message.getBody()));
				});		
	}

	public static AbstractMessageListenerContainer startListening(RabbitAdmin rabbitAdmin, Queue queue, Exchange exchange, String key, MessageListener messageListener) {
		rabbitAdmin.declareBinding(BindingBuilder.bind(queue).to(exchange).with(key).noargs());
		SimpleMessageListenerContainer listener = new SimpleMessageListenerContainer(rabbitAdmin.getRabbitTemplate().getConnectionFactory());
		listener.addQueues(queue);
		listener.setMessageListener(messageListener);
		listener.start();
	
		return listener;
	}
*/
}

}
