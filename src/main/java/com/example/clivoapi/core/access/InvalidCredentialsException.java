package com.example.clivoapi.core.access;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("email or password does not match an active user");
    }
}
