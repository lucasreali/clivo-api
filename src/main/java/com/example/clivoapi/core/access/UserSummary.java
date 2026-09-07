package com.example.clivoapi.core.access;

import java.util.UUID;

public record UserSummary(UUID id, String name, EmailAddress email, Role role, boolean active) {
}
