package com.example.clivoapi.common.extension;

public record ModuleActivationProposal(ModuleCode module, ActivationIntent intent) {

    public static ModuleActivationProposal toActivate(ModuleCode module) {
        return new ModuleActivationProposal(module, ActivationIntent.ACTIVATION);
    }

    public static ModuleActivationProposal toDeactivate(ModuleCode module) {
        return new ModuleActivationProposal(module, ActivationIntent.DEACTIVATION);
    }

    public boolean isActivation() {
        return intent == ActivationIntent.ACTIVATION;
    }

    public boolean isDeactivation() {
        return intent == ActivationIntent.DEACTIVATION;
    }
}
