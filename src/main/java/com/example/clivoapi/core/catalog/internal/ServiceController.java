package com.example.clivoapi.core.catalog.internal;

import com.example.clivoapi.core.catalog.CatalogService;
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
class ServiceController {

    private final CatalogService catalogue;

    ServiceController(CatalogService catalogue) {
        this.catalogue = catalogue;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ServiceView register(@Valid @RequestBody ServiceRequest request) {
        return ServiceView.of(catalogue.register(request.toDetails()));
    }

    @GetMapping
    List<ServiceView> list() {
        return catalogue.findAll().stream().map(ServiceView::of).toList();
    }

    @GetMapping("/{id}")
    ServiceView findOne(@PathVariable Long id) {
        return ServiceView.of(catalogue.findOne(id));
    }

    @PutMapping("/{id}")
    ServiceView describe(@PathVariable Long id, @Valid @RequestBody ServiceRequest request) {
        return ServiceView.of(catalogue.describe(id, request.toDetails()));
    }

    @PostMapping("/{id}/deactivation")
    ServiceView deactivate(@PathVariable Long id) {
        return ServiceView.of(catalogue.deactivate(id));
    }
}
