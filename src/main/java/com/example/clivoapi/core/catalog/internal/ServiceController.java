package com.example.clivoapi.core.catalog.internal;

import com.example.clivoapi.core.catalog.CatalogService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/services")
@Tag(name = "Services", description = "The procedures the clinic sells")
class ServiceController {

    private final CatalogService catalogue;

    ServiceController(CatalogService catalogue) {
        this.catalogue = catalogue;
    }

    @Operation(operationId = "registerService", summary = "Register a service in the catalogue")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ServiceView register(@Valid @RequestBody ServiceRequest request) {
        return ServiceView.of(catalogue.register(request.toDetails()));
    }

    @Operation(operationId = "listServices", summary = "List the catalogue")
    @GetMapping
    List<ServiceView> list() {
        return catalogue.findAll().stream().map(ServiceView::of).toList();
    }

    @Operation(operationId = "getService", summary = "Read one service")
    @GetMapping("/{id}")
    ServiceView findOne(@PathVariable Long id) {
        return ServiceView.of(catalogue.findOne(id));
    }

    @Operation(operationId = "describeService", summary = "Redescribe a service")
    @PutMapping("/{id}")
    ServiceView describe(@PathVariable Long id, @Valid @RequestBody ServiceRequest request) {
        return ServiceView.of(catalogue.describe(id, request.toDetails()));
    }

    @Operation(operationId = "deactivateService", summary = "Deactivate a service, keeping its history")
    @PostMapping("/{id}/deactivation")
    ServiceView deactivate(@PathVariable Long id) {
        return ServiceView.of(catalogue.deactivate(id));
    }
}
