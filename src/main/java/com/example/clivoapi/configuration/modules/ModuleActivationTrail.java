package com.example.clivoapi.configuration.modules;

import com.example.clivoapi.configuration.modules.internal.ModuleActivationChangeRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

@Component
class ModuleActivationTrail {

    private final ModuleActivationChangeRepository changes;
    private final AuditorAware<UUID> auditor;

    ModuleActivationTrail(ModuleActivationChangeRepository changes, AuditorAware<UUID> auditor) {
        this.changes = changes;
        this.auditor = auditor;
    }

    UUID author() {
        return auditor.getCurrentAuditor().orElse(null);
    }

    void record(ModuleActivationChange change) {
        changes.save(change);
    }

    List<ModuleActivationRecord> entries() {
        return changes.findAllByOrderByChangedAtDesc().stream()
                .map(ModuleActivationChange::asRecord)
                .toList();
    }
}
