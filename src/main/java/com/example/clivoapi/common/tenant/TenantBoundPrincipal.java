package com.example.clivoapi.common.tenant;

import java.util.Optional;
import java.util.UUID;

public interface TenantBoundPrincipal {

    Optional<UUID> tenant();
}
