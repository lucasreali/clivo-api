package com.example.clivoapi.modules.sessionpackage.internal;

import com.example.clivoapi.common.extension.RequiresModule;
import com.example.clivoapi.modules.sessionpackage.SessionPackageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/session-packages")
@RequiresModule("sessionpackage")
@Tag(name = "Session packages", description = "Sessions sold up front and consumed over time. Requires the `sessionpackage` module")
class SessionPackageController {

    private final SessionPackageService packages;

    SessionPackageController(SessionPackageService packages) {
        this.packages = packages;
    }

    @Operation(operationId = "sellSessionPackage", summary = "Sell a package of sessions to a customer")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    PackageView sell(@Valid @RequestBody PackageRequest request) {
        return PackageView.of(packages.sell(request.toPurchase()));
    }

    @Operation(operationId = "listCustomerSessionPackages", summary = "List a customer's packages")
    @GetMapping
    List<PackageView> of(@RequestParam UUID customerId) {
        return packages.of(customerId).stream().map(PackageView::of).toList();
    }

    @Operation(operationId = "getSessionPackage", summary = "Read one package with the sessions already used")
    @GetMapping("/{id}")
    PackageView findOne(@PathVariable UUID id) {
        return PackageView.of(packages.findOne(id));
    }

    @Operation(operationId = "cancelSessionPackage", summary = "Cancel a package, releasing the sessions not used")
    @PostMapping("/{id}/cancellation")
    PackageView cancel(@PathVariable UUID id) {
        return PackageView.of(packages.cancel(id));
    }
}
