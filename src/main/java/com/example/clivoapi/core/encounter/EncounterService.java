package com.example.clivoapi.core.encounter;

import com.example.clivoapi.common.exception.ResourceNotFoundException;
import com.example.clivoapi.common.extension.RecordValues;
import com.example.clivoapi.core.encounter.internal.EncounterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EncounterService {

    private final EncounterRepository encounters;
    private final EncounterAssembler assembler;

    EncounterService(EncounterRepository encounters, EncounterAssembler assembler) {
        this.encounters = encounters;
        this.assembler = assembler;
    }

    public EncounterSnapshot open(EncounterOpening opening) {
        return encounters.save(assembler.assemble(opening)).snapshot();
    }

    public EncounterSnapshot fill(Long id, RecordValues values) {
        Encounter encounter = encounterOf(id);
        encounter.fill(values);
        return encounters.save(encounter).snapshot();
    }

    public EncounterSnapshot complete(Long id) {
        Encounter encounter = encounterOf(id);
        encounter.complete();
        return encounters.save(encounter).snapshot();
    }

    @Transactional(readOnly = true)
    public EncounterSnapshot findOne(Long id) {
        return encounterOf(id).snapshot();
    }

    private Encounter encounterOf(Long id) {
        return encounters.findById(id).orElseThrow(() -> new ResourceNotFoundException("Encounter", id));
    }
}
