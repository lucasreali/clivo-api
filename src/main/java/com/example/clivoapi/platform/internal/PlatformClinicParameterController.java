package com.example.clivoapi.platform.internal;

import com.example.clivoapi.common.extension.ParameterCode;
import com.example.clivoapi.common.extension.ParameterValue;
import com.example.clivoapi.configuration.parameter.ClinicParameterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(PlatformPath.ONE_CLINIC + "/parameters")
@Tag(name = "Platform clinic parameters", description = "Parameters of one clinic, driven by the platform")
class PlatformClinicParameterController {

    private final ClinicParameterService parameters;

    PlatformClinicParameterController(ClinicParameterService parameters) {
        this.parameters = parameters;
    }

    @Operation(operationId = "listClinicParameters", summary = "List the parameters in effect for one clinic")
    @GetMapping
    List<PlatformParameterView> listEffective() {
        return parameters.effectiveParameters().stream().map(PlatformParameterView::of).toList();
    }

    @Operation(operationId = "changeClinicParameter", summary = "Change one parameter of one clinic")
    @PutMapping("/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void change(@PathVariable String code, @Valid @RequestBody PlatformParameterChangeRequest request) {
        parameters.change(new ParameterCode(code), ParameterValue.of(request.value()));
    }
}
