package com.example.clivoapi.platform;

import com.example.clivoapi.common.tenant.TenantSnapshot;
import com.example.clivoapi.core.access.UserSummary;

public record ProvisionedClinic(TenantSnapshot clinic, UserSummary manager) {
}
