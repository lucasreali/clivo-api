package com.example.clivoapi.core.access;

public record UserSummary(Long id, String name, EmailAddress email, Role role, boolean active) {
}
