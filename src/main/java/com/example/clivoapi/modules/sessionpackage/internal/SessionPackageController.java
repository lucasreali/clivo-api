package com.example.clivoapi.modules.sessionpackage.internal;

import com.example.clivoapi.common.extension.RequiresModule;
import com.example.clivoapi.modules.sessionpackage.SessionPackageService;
import jakarta.validation.Valid;
import java.util.List;
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
class SessionPackageController {

    private final SessionPackageService packages;

    SessionPackageController(SessionPackageService packages) {
        this.packages = packages;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    PackageView sell(@Valid @RequestBody PackageRequest request) {
        return PackageView.of(packages.sell(request.toPurchase()));
    }

    @GetMapping
    List<PackageView> of(@RequestParam Long customerId) {
        return packages.of(customerId).stream().map(PackageView::of).toList();
    }

    @GetMapping("/{id}")
    PackageView findOne(@PathVariable Long id) {
        return PackageView.of(packages.findOne(id));
    }

    @PostMapping("/{id}/cancellation")
    PackageView cancel(@PathVariable Long id) {
        return PackageView.of(packages.cancel(id));
    }
}
