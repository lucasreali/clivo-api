package com.example.clivoapi.modules.sessionpackage.internal;

import com.example.clivoapi.common.extension.CompletedEncounter;
import com.example.clivoapi.common.extension.EncounterCompletionListener;
import com.example.clivoapi.common.extension.ModuleActivationState;
import com.example.clivoapi.common.extension.ModuleCode;
import com.example.clivoapi.modules.sessionpackage.SessionPackageService;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(PackageSessionConsumer.AFTER_INVOICING)
class PackageSessionConsumer implements EncounterCompletionListener {

    static final int AFTER_INVOICING = 100;

    private static final ModuleCode SESSION_PACKAGE = new ModuleCode("sessionpackage");

    private final ModuleActivationState activation;
    private final SessionPackageService packages;

    PackageSessionConsumer(ModuleActivationState activation, SessionPackageService packages) {
        this.activation = activation;
        this.packages = packages;
    }

    @Override
    public void onCompleted(CompletedEncounter encounter) {
        if (activation.isActive(SESSION_PACKAGE)) {
            packages.consumeFor(encounter);
        }
    }
}
