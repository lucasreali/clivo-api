package com.example.clivoapi.configuration.modules;

import com.example.clivoapi.common.extension.ActivationIntent;
import com.example.clivoapi.common.extension.ModuleCode;
import com.example.clivoapi.common.tenant.TenantScopedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tenant_module")
public class ModuleActivation extends TenantScopedEntity {

    @Id
    @Column(name = "module_code")
    private String moduleCode;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "enabled_at")
    private Instant enabledAt;

    @Column(name = "enabled_by")
    private UUID enabledBy;

    protected ModuleActivation() {
    }

    public ModuleActivation(ModuleCode module) {
        this.moduleCode = module.value();
        this.enabled = false;
    }

    public ModuleCode module() {
        return new ModuleCode(moduleCode);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public ModuleActivationChange enable(UUID author) {
        enabled = true;
        enabledAt = Instant.now();
        enabledBy = author;
        return new ModuleActivationChange(module(), ActivationIntent.ACTIVATION, author);
    }

    public ModuleActivationChange disable(UUID author) {
        enabled = false;
        enabledAt = null;
        enabledBy = null;
        return new ModuleActivationChange(module(), ActivationIntent.DEACTIVATION, author);
    }
}
