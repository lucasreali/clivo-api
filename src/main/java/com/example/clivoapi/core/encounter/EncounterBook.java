package com.example.clivoapi.core.encounter;

import com.example.clivoapi.common.exception.ResourceNotFoundException;
import com.example.clivoapi.core.encounter.internal.EncounterRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
public class EncounterBook {

    private final EncounterRepository encounters;

    EncounterBook(EncounterRepository encounters) {
        this.encounters = encounters;
    }

    public Encounter reference(Long id) {
        return encounters.findById(id).orElseThrow(() -> new ResourceNotFoundException("Encounter", id));
    }
}
