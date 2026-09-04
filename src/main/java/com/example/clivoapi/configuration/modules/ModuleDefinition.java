package com.example.clivoapi.configuration.modules;

import com.example.clivoapi.common.extension.ModuleCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Optional;

@Entity
@Table(name = "module")
public class ModuleDefinition {

    @Id
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(name = "requires_module")
    private String requiresModule;

    protected ModuleDefinition() {
    }

    public ModuleCode code() {
        return new ModuleCode(code);
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public Optional<ModuleCode> dependency() {
        return Optional.ofNullable(requiresModule).map(ModuleCode::new);
    }

    public boolean hasCode(ModuleCode module) {
        return code.equals(module.value());
    }

    public boolean dependsOn(ModuleCode module) {
        return module.value().equals(requiresModule);
    }
}
