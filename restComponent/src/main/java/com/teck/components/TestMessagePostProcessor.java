package com.teck.components;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;

public class TestMessagePostProcessor implements MessagePostProcessor {

    /* If need to configure the message processor
    private final Integer ttl;

    public MyMessagePostProcessor(final Integer ttl) {
        this.ttl = ttl;
    }
    */
    
    @Override
    public Message postProcessMessage(final Message message) throws AmqpException {
        message.getMessageProperties().getHeaders().put("KILROY", "wuz here");
        return message;
    }
}