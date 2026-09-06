package com.example.clivoapi.modules.dependent.internal;

import com.example.clivoapi.common.extension.RequiresModule;
import com.example.clivoapi.modules.dependent.DependentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiresModule("dependent")
@Tag(name = "Dependents", description = "People attended under another customer's responsibility. Requires the `dependent` module")
class DependentController {

    private final DependentService dependents;

    DependentController(DependentService dependents) {
        this.dependents = dependents;
    }

    @Operation(operationId = "registerDependent", summary = "Register a dependent under a customer")
    @PostMapping("/api/customers/{customerId}/dependents")
    @ResponseStatus(HttpStatus.CREATED)
    DependentView register(@PathVariable Long customerId, @Valid @RequestBody DependentRequest request) {
        return DependentView.of(dependents.register(customerId, request.toDetails()));
    }

    @Operation(operationId = "listCustomerDependents", summary = "List the dependents a customer answers for")
    @GetMapping("/api/customers/{customerId}/dependents")
    List<DependentView> caredForBy(@PathVariable Long customerId) {
        return dependents.caredForBy(customerId).stream().map(DependentView::of).toList();
    }

    @Operation(operationId = "getDependent", summary = "Read one dependent")
    @GetMapping("/api/dependents/{id}")
    DependentView findOne(@PathVariable Long id) {
        return DependentView.of(dependents.findOne(id));
    }

    @Operation(operationId = "describeDependent", summary = "Redescribe a dependent")
    @PutMapping("/api/dependents/{id}")
    DependentView describe(@PathVariable Long id, @Valid @RequestBody DependentRequest request) {
        return DependentView.of(dependents.describe(id, request.toDetails()));
    }

    @Operation(operationId = "deactivateDependent", summary = "Deactivate a dependent, keeping their history")
    @PostMapping("/api/dependents/{id}/deactivation")
    DependentView deactivate(@PathVariable Long id) {
        return DependentView.of(dependents.deactivate(id));
    }
}
