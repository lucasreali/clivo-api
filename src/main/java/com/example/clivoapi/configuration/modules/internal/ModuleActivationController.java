package com.example.clivoapi.configuration.modules.internal;

import com.example.clivoapi.common.extension.ModuleCode;
import com.example.clivoapi.configuration.modules.ModuleActivationService;
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
@RequestMapping("/api/modules")
class ModuleActivationController {

    private final ModuleActivationService modules;

    ModuleActivationController(ModuleActivationService modules) {
        this.modules = modules;
    }

    @GetMapping
    List<ModuleView> listCatalog() {
        return modules.statusOfAll().stream().map(ModuleView::of).toList();
    }

    @PutMapping("/{code}/activation")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void activate(@PathVariable String code) {
        modules.activate(new ModuleCode(code));
    }

    @DeleteMapping("/{code}/activation")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deactivate(@PathVariable String code) {
        modules.deactivate(new ModuleCode(code));
    }
}
