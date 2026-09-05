package com.example.clivoapi.modules.notification.internal;

import com.example.clivoapi.modules.notification.MessageGateway;
import com.example.clivoapi.modules.notification.OutboundMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
class LoggingMessageGateway implements MessageGateway {

    private static final Logger LOG = LoggerFactory.getLogger(LoggingMessageGateway.class);

    @Override
    public boolean deliver(OutboundMessage message) {
        LOG.info("{} to {}: {}", message.channel(), message.recipient(), message.text());
        return true;
    }
}
