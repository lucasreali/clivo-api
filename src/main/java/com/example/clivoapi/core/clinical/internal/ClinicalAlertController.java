package com.example.clivoapi.core.clinical.internal;

import com.example.clivoapi.core.access.AuthenticatedUser;
import com.example.clivoapi.core.access.Role;
import com.example.clivoapi.core.clinical.ClinicalAlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/clinical-alerts")
@Tag(name = "Clinical alerts", description = "Standing clinical warnings a practitioner keeps on a customer")
class ClinicalAlertController {

    private final ClinicalAlertService alerts;

    ClinicalAlertController(ClinicalAlertService alerts) {
        this.alerts = alerts;
    }

    @Operation(operationId = "recordClinicalAlert", summary = "Record a standing clinical alert on a customer")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    AlertView record(
            @RequestParam UUID customerId,
            @Valid @RequestBody AlertRequest request,
            @AuthenticationPrincipal AuthenticatedUser viewer) {
        return AlertView.of(alerts.record(customerId, request.toNote(), roleOf(viewer)));
    }

    @Operation(operationId = "listClinicalAlerts", summary = "List the standing clinical alerts of a customer")
    @GetMapping
    List<AlertView> of(@RequestParam UUID customerId, @AuthenticationPrincipal AuthenticatedUser viewer) {
        return alerts.visibleTo(customerId, roleOf(viewer)).stream().map(AlertView::of).toList();
    }

    @Operation(operationId = "rewriteClinicalAlert", summary = "Rewrite a standing clinical alert")
    @PutMapping("/{id}")
    AlertView rewrite(
            @PathVariable UUID id,
            @Valid @RequestBody AlertRequest request,
            @AuthenticationPrincipal AuthenticatedUser viewer) {
        return AlertView.of(alerts.rewrite(id, request.toNote(), roleOf(viewer)));
    }

    @Operation(operationId = "withdrawClinicalAlert", summary = "Withdraw a standing clinical alert")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void withdraw(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser viewer) {
        alerts.withdraw(id, roleOf(viewer));
    }

    private Role roleOf(AuthenticatedUser viewer) {
        return Optional.ofNullable(viewer).map(AuthenticatedUser::role).orElse(Role.RECEPTION);
    }
}
