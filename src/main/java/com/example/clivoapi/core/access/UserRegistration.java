package com.example.clivoapi.core.access;

public record UserRegistration(String name, EmailAddress email, RawPassword password, Role role) {
}
