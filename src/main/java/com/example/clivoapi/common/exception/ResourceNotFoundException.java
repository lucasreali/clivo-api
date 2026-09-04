package com.example.clivoapi.common.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, Object identifier) {
        super("%s %s not found".formatted(resource, identifier));
    }
}
