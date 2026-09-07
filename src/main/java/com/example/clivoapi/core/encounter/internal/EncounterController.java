package com.example.clivoapi.core.encounter.internal;

import com.example.clivoapi.core.access.AuthenticatedUser;
import com.example.clivoapi.core.access.Role;
import com.example.clivoapi.core.encounter.EncounterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
@Tag(name = "Encounters", description = "Appointments turned into attended visits and their clinical records")
class EncounterController {

    private final EncounterService encounters;

    EncounterController(EncounterService encounters) {
        this.encounters = encounters;
    }

    @Operation(operationId = "openEncounter", summary = "Open an encounter over a published record template")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    EncounterView open(@Valid @RequestBody EncounterRequest request) {
        return EncounterView.of(encounters.open(request.toOpening()));
    }

    @Operation(operationId = "getEncounter", summary = "Read one encounter; clinical content depends on the caller's role")
    @GetMapping("/{id}")
    EncounterView findOne(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser viewer) {
        return EncounterView.of(encounters.findOne(id, roleOf(viewer)));
    }

    @Operation(operationId = "listCustomerEncounters", summary = "List a customer's encounters; clinical content depends on the caller's role")
    @GetMapping
    List<EncounterHistoryView> history(
            @RequestParam UUID customerId, @AuthenticationPrincipal AuthenticatedUser viewer) {
        return encounters.historyOf(customerId, roleOf(viewer)).stream()
                .map(EncounterHistoryView::of)
                .toList();
    }

    @Operation(operationId = "fillEncounterRecord", summary = "Fill the encounter's record with the template's field values")
    @PutMapping("/{id}/record")
    EncounterView fill(@PathVariable UUID id, @RequestBody RecordFillingRequest request) {
        return EncounterView.of(encounters.fill(id, request.toValues()));
    }

    @Operation(operationId = "completeEncounter", summary = "Complete an encounter, closing its record and billing it")
    @PostMapping("/{id}/completion")
    EncounterView complete(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser viewer) {
        return EncounterView.of(encounters.complete(id, roleOf(viewer)));
    }

    private Role roleOf(AuthenticatedUser viewer) {
        return Optional.ofNullable(viewer).map(AuthenticatedUser::role).orElse(Role.RECEPTION);
    }
}
