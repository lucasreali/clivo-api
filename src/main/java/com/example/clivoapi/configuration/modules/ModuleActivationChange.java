package com.example.clivoapi.configuration.modules;

import static org.hibernate.annotations.UuidGenerator.Style.VERSION_7;

import com.example.clivoapi.common.extension.ActivationIntent;
import com.example.clivoapi.common.extension.ModuleCode;
import com.example.clivoapi.common.tenant.TenantScopedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "tenant_module_history")
public class ModuleActivationChange extends TenantScopedEntity {

    @Id
    @UuidGenerator(style = VERSION_7)
    private UUID id;

    @Column(name = "module_code", nullable = false, updatable = false)
    private String moduleCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private ActivationIntent action;

    @Column(name = "changed_at", nullable = false, updatable = false)
    private Instant changedAt;

    @Column(name = "changed_by", updatable = false)
    private UUID changedBy;

    protected ModuleActivationChange() {
    }

    ModuleActivationChange(ModuleCode module, ActivationIntent action, UUID author) {
        this.moduleCode = module.value();
        this.action = action;
        this.changedAt = Instant.now();
        this.changedBy = author;
    }

    ModuleActivationRecord asRecord() {
        return new ModuleActivationRecord(new ModuleCode(moduleCode), action, changedAt, changedBy);
    }
}
