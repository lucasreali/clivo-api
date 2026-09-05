package com.example.clivoapi.core.encounter;

import com.example.clivoapi.common.extension.CompletedEncounter;
import com.example.clivoapi.common.extension.EncounterCompletionListener;
import java.util.List;

class EncounterCompletion {

    private final List<EncounterCompletionListener> listeners;

    EncounterCompletion(List<EncounterCompletionListener> listeners) {
        this.listeners = List.copyOf(listeners);
    }

    void announce(CompletedEncounter encounter) {
        listeners.forEach(listener -> listener.onCompleted(encounter));
    }
}
