package com.example.clivoapi.platform.internal;

import com.example.clivoapi.common.extension.ModuleCode;
import com.example.clivoapi.configuration.modules.ModuleActivationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(PlatformPath.ONE_CLINIC + "/modules")
@Tag(name = "Platform clinic modules", description = "Module activation of one clinic, driven by the platform")
class PlatformClinicModuleController {

    private final ModuleActivationService modules;

    PlatformClinicModuleController(ModuleActivationService modules) {
        this.modules = modules;
    }

    @Operation(operationId = "listClinicModules", summary = "List every module with its status for one clinic")
    @GetMapping
    List<PlatformModuleView> listCatalog() {
        return modules.statusOfAll().stream().map(PlatformModuleView::of).toList();
    }

    @Operation(operationId = "listClinicModuleHistory", summary = "Read the activation history of one clinic")
    @GetMapping("/history")
    List<PlatformModuleChangeView> listHistory() {
        return modules.history().stream().map(PlatformModuleChangeView::of).toList();
    }

    @Operation(operationId = "activateClinicModule", summary = "Activate a module for one clinic")
    @PutMapping("/{code}/activation")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void activate(@PathVariable String code) {
        modules.activate(new ModuleCode(code));
    }

    @Operation(operationId = "deactivateClinicModule", summary = "Deactivate a module for one clinic")
    @DeleteMapping("/{code}/activation")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deactivate(@PathVariable String code) {
        modules.deactivate(new ModuleCode(code));
    }
}
