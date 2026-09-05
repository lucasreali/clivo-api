package com.example.clivoapi.core.encounter.internal;

import com.example.clivoapi.core.access.AuthenticatedUser;
import com.example.clivoapi.core.access.Role;
import com.example.clivoapi.core.encounter.EncounterService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    EncounterView findOne(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser viewer) {
        return EncounterView.of(encounters.findOne(id, roleOf(viewer)));
    }

    @GetMapping
    List<EncounterHistoryView> history(
            @RequestParam Long customerId, @AuthenticationPrincipal AuthenticatedUser viewer) {
        return encounters.historyOf(customerId, roleOf(viewer)).stream()
                .map(EncounterHistoryView::of)
                .toList();
    }

    @PutMapping("/{id}/record")
    EncounterView fill(@PathVariable Long id, @RequestBody RecordFillingRequest request) {
        return EncounterView.of(encounters.fill(id, request.toValues()));
    }

    @PostMapping("/{id}/completion")
    EncounterView complete(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser viewer) {
        return EncounterView.of(encounters.complete(id, roleOf(viewer)));
    }

    private Role roleOf(AuthenticatedUser viewer) {
        return Optional.ofNullable(viewer).map(AuthenticatedUser::role).orElse(Role.RECEPTION);
    }
}
