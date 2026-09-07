package com.example.clivoapi.core.access.internal;

import com.example.clivoapi.common.extension.ModuleCode;
import com.example.clivoapi.core.access.ModuleAccessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(UserModuleController.PATH)
@Tag(name = "User modules", description = "Which modules of the clinic each user reaches, decided by the manager")
class UserModuleController {

    static final String PATH = "/api/users/{userId}/modules";

    private final ModuleAccessService access;

    UserModuleController(ModuleAccessService access) {
        this.access = access;
    }

    @Operation(operationId = "listUserModules", summary = "List the modules granted to a user")
    @GetMapping
    List<String> list(@PathVariable UUID userId) {
        return codesOf(access.modulesGrantedTo(userId));
    }

    @Operation(operationId = "grantUserModule", summary = "Grant a user access to a module active in the clinic")
    @PutMapping("/{code}")
    List<String> grant(@PathVariable UUID userId, @PathVariable String code) {
        return codesOf(access.grant(userId, new ModuleCode(code)));
    }

    @Operation(operationId = "revokeUserModule", summary = "Revoke a user's access to a module")
    @DeleteMapping("/{code}")
    List<String> revoke(@PathVariable UUID userId, @PathVariable String code) {
        return codesOf(access.revoke(userId, new ModuleCode(code)));
    }

    private List<String> codesOf(List<ModuleCode> modules) {
        return modules.stream().map(ModuleCode::value).toList();
    }
}
