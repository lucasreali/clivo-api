package com.example.clivoapi.modules.notification;

public interface MessageGateway {

    boolean deliver(OutboundMessage message);
}
