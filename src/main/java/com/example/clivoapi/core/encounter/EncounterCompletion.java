package com.example.clivoapi.core.encounter;

import com.example.clivoapi.common.extension.CompletedEncounter;
import com.example.clivoapi.common.extension.EncounterCompletionListener;
import com.example.clivoapi.core.access.AccessService;
import com.example.clivoapi.core.access.AppUser;
import java.util.List;
import java.util.Optional;

class EncounterCompletion {

    private final List<EncounterCompletionListener> listeners;
    private final AccessService access;

    EncounterCompletion(List<EncounterCompletionListener> listeners, AccessService access) {
        this.listeners = List.copyOf(listeners);
        this.access = access;
    }

    Optional<AppUser> signer() {
        return access.whoeverIsSignedIn();
    }

    void announce(CompletedEncounter encounter) {
        listeners.forEach(listener -> listener.onCompleted(encounter));
    }
}
