package com.example.clivoapi.core.access;

public record SignInAttempt(EmailAddress email, RawPassword password) {
}
