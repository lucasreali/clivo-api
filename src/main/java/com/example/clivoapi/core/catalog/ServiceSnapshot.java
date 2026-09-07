package com.example.clivoapi.core.catalog;

import java.util.UUID;

public record ServiceSnapshot(UUID id, ServiceDetails details, ServiceStatus status) {
}
