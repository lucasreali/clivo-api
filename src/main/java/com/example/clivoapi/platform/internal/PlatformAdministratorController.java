package com.example.clivoapi.platform.internal;

import com.example.clivoapi.core.access.AccessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(PlatformAdministratorController.PATH)
@Tag(name = "Platform administrators", description = "The people who administer the platform itself")
class PlatformAdministratorController {

    static final String PATH = PlatformPath.ROOT + "/administrators";

    private final AccessService access;

    PlatformAdministratorController(AccessService access) {
        this.access = access;
    }

    @Operation(operationId = "registerPlatformAdministrator", summary = "Register another platform administrator")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ClinicUserView register(@Valid @RequestBody PlatformAdministratorRequest request) {
        return ClinicUserView.of(access.register(request.toRegistration()));
    }
}
