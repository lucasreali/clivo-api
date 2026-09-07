package com.example.clivoapi.common.tenant;

import java.util.UUID;

public record TenantIdentity(UUID id, String code, String name) {
}
