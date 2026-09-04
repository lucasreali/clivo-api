package com.example.clivoapi.configuration.parameter.internal;

import com.example.clivoapi.common.extension.ClinicParameters;
import com.example.clivoapi.common.extension.ParameterCode;
import com.example.clivoapi.common.extension.ParameterValue;
import com.example.clivoapi.configuration.parameter.ClinicParameterService;
import org.springframework.stereotype.Component;

@Component
class StoredClinicParameters implements ClinicParameters {

    private final ClinicParameterService parameters;

    StoredClinicParameters(ClinicParameterService parameters) {
        this.parameters = parameters;
    }

    @Override
    public ParameterValue valueOf(ParameterCode parameter) {
        return parameters.valueOf(parameter);
    }
}
