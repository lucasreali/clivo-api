package com.example.clivoapi.core.access;

import static org.hibernate.annotations.UuidGenerator.Style.VERSION_7;

import com.example.clivoapi.common.extension.ModuleCode;
import com.example.clivoapi.common.tenant.TenantScopedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "module_grant")
public class ModuleGrant extends TenantScopedEntity {

    @Id
    @UuidGenerator(style = VERSION_7)
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "module_code", nullable = false, updatable = false)
    private String moduleCode;

    @Column(name = "granted_at", nullable = false, updatable = false)
    private Instant grantedAt;

    @Column(name = "granted_by")
    private UUID grantedBy;

    protected ModuleGrant() {
    }

    public ModuleGrant(UUID userId, ModuleCode module, UUID author) {
        this.userId = userId;
        this.moduleCode = module.value();
        this.grantedAt = Instant.now();
        this.grantedBy = author;
    }

    public ModuleCode module() {
        return new ModuleCode(moduleCode);
    }
}
