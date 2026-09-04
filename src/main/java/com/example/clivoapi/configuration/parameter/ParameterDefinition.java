package com.example.clivoapi.configuration.parameter;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.extension.ModuleCode;
import com.example.clivoapi.common.extension.ParameterCode;
import com.example.clivoapi.common.extension.ParameterValue;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;
import java.util.Optional;

@Entity
@Table(name = "parameter")
public class ParameterDefinition {

    private static final ParameterConstraints UNLIMITED = new ParameterConstraints();

    @Id
    private String code;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", nullable = false)
    private ParameterDataType dataType;

    @Embedded
    private ParameterConstraints constraints;

    @Column(name = "default_value", nullable = false)
    private String defaultValue;

    @Column(name = "module_code")
    private String moduleCode;

    protected ParameterDefinition() {
    }

    public ParameterCode code() {
        return new ParameterCode(code);
    }

    public String name() {
        return name;
    }

    public ParameterValue defaultValue() {
        return new ParameterValue(defaultValue);
    }

    public Optional<ModuleCode> module() {
        return Optional.ofNullable(moduleCode).map(ModuleCode::new);
    }

    public String acceptedValues() {
        return dataType.acceptedValues(constraints());
    }

    public void requireAcceptable(ParameterValue value) {
        if (dataType.accepts(value, constraints())) {
            return;
        }
        throw new BusinessException("parameter %s accepts %s".formatted(code, acceptedValues()));
    }

    private ParameterConstraints constraints() {
        return Objects.requireNonNullElse(constraints, UNLIMITED);
    }
}
