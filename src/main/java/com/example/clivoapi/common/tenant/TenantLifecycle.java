package com.example.clivoapi.common.tenant;

import com.example.clivoapi.common.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.Instant;
import java.util.Optional;

@Embeddable
public record TenantLifecycle(
        @Enumerated(EnumType.STRING) @Column(name = "status", nullable = false, length = 12) TenantStatus status,
        @Column(name = "status_reason", length = 200) String statusReason,
        @Column(name = "status_changed_at", nullable = false) Instant statusChangedAt) {

    static TenantLifecycle opened() {
        return new TenantLifecycle(TenantStatus.ACTIVE, null, Instant.now());
    }

    boolean isActive() {
        return status == TenantStatus.ACTIVE;
    }

    TenantLifecycle activated() {
        requireNotClosed();
        return new TenantLifecycle(TenantStatus.ACTIVE, null, Instant.now());
    }

    TenantLifecycle suspended(String reason) {
        requireNotClosed();
        return new TenantLifecycle(TenantStatus.SUSPENDED, required(reason), Instant.now());
    }

    TenantLifecycle closed(String reason) {
        requireNotClosed();
        return new TenantLifecycle(TenantStatus.CLOSED, required(reason), Instant.now());
    }

    public Optional<String> reason() {
        return Optional.ofNullable(statusReason);
    }

    private void requireNotClosed() {
        if (status != TenantStatus.CLOSED) {
            return;
        }
        throw new BusinessException("a closed clinic no longer changes status");
    }

    private String required(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new BusinessException("a reason is required to take a clinic out of service");
        }
        return reason.trim();
    }
}
