package com.example.clivoapi.core.encounter;

import java.util.UUID;

public record AttendingPractitioner(UUID id, String name, String license) {
}
