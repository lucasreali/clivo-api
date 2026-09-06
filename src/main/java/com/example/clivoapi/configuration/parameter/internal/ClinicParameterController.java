package com.example.clivoapi.configuration.parameter.internal;

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
@RequestMapping("/api/parameters")
@Tag(name = "Parameters", description = "Settings that tune the clinic's behaviour without a code change")
class ClinicParameterController {

    private final ClinicParameterService parameters;

    ClinicParameterController(ClinicParameterService parameters) {
        this.parameters = parameters;
    }

    @Operation(operationId = "listParameters", summary = "List the parameters in effect, defaults included")
    @GetMapping
    List<ParameterView> listEffective() {
        return parameters.effectiveParameters().stream().map(ParameterView::of).toList();
    }

    @Operation(operationId = "changeParameter", summary = "Change one parameter for the current clinic")
    @PutMapping("/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void change(@PathVariable String code, @Valid @RequestBody ParameterChangeRequest request) {
        parameters.change(new ParameterCode(code), ParameterValue.of(request.value()));
    }
}
