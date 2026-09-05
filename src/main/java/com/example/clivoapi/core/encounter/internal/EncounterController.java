package com.example.clivoapi.core.encounter.internal;

import com.example.clivoapi.core.encounter.EncounterService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/encounters")
class EncounterController {

    private final EncounterService encounters;

    EncounterController(EncounterService encounters) {
        this.encounters = encounters;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    EncounterView open(@Valid @RequestBody EncounterRequest request) {
        return EncounterView.of(encounters.open(request.toOpening()));
    }

    @GetMapping("/{id}")
    EncounterView findOne(@PathVariable Long id) {
        return EncounterView.of(encounters.findOne(id));
    }

    @PutMapping("/{id}/record")
    EncounterView fill(@PathVariable Long id, @RequestBody RecordFillingRequest request) {
        return EncounterView.of(encounters.fill(id, request.toValues()));
    }

    @PostMapping("/{id}/completion")
    EncounterView complete(@PathVariable Long id) {
        return EncounterView.of(encounters.complete(id));
    }
}
