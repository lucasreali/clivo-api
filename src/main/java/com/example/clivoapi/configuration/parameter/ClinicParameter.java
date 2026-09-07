package com.example.clivoapi.configuration.parameter;

import com.example.clivoapi.common.extension.ParameterCode;
import com.example.clivoapi.common.extension.ParameterValue;
import com.example.clivoapi.common.tenant.TenantScopedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tenant_parameter")
public class ClinicParameter extends TenantScopedEntity {

    @Id
    @Column(name = "parameter_code")
    private String parameterCode;

    @Column(nullable = false)
    private String value;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "updated_by")
    private UUID updatedBy;

    protected ClinicParameter() {
    }

    public ClinicParameter(ParameterCode parameter) {
        this.parameterCode = parameter.value();
    }

    public ParameterCode code() {
        return new ParameterCode(parameterCode);
    }

    public ParameterValue value() {
        return new ParameterValue(value);
    }

    public void changeTo(ParameterValue newValue, UUID userId) {
        value = newValue.asText();
        updatedAt = Instant.now();
        updatedBy = userId;
    }
}
