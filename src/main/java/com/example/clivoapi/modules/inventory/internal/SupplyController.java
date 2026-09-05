package com.example.clivoapi.modules.inventory.internal;

import com.example.clivoapi.common.extension.RequiresModule;
import com.example.clivoapi.core.encounter.EncounterService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiresModule("inventory")
class SupplyController {

    private final EncounterService encounters;

    SupplyController(EncounterService encounters) {
        this.encounters = encounters;
    }

    @PostMapping("/api/encounters/{encounterId}/supplies")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void use(@PathVariable Long encounterId, @Valid @RequestBody SupplyRequest request) {
        encounters.useSupplies(encounterId, request.productId(), request.quantity());
    }
}
