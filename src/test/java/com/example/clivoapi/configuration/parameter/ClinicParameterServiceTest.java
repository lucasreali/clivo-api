package com.example.clivoapi.configuration.parameter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.clivoapi.common.DatabaseTest;
import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.exception.ResourceNotFoundException;
import com.example.clivoapi.common.extension.ClinicParameters;
import com.example.clivoapi.common.extension.ParameterCode;
import com.example.clivoapi.common.extension.ParameterValue;
import com.example.clivoapi.common.tenant.Tenant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ClinicParameterServiceTest extends DatabaseTest {

    private static final ParameterCode REMINDER_LEAD_HOURS = new ParameterCode("reminder_lead_hours");
    private static final ParameterCode ROLE_MODEL = new ParameterCode("role_model");
    private static final ParameterCode BLOCK_EXPIRED_BATCH = new ParameterCode("block_expired_batch");

    @Autowired
    private ClinicParameterService parameters;

    @Autowired
    private ClinicParameters port;

    @Test
    void valueOutOfRangeIsRefusedAndTheStoredValueRemains() {
        Tenant clinic = createTenant("TEST-PARAM-RANGE");
        inTenant(clinic, () -> parameters.change(REMINDER_LEAD_HOURS, ParameterValue.of("12")));

        assertThatThrownBy(() -> inTenant(clinic, () -> parameters.change(REMINDER_LEAD_HOURS, ParameterValue.of("200"))))
                .isInstanceOf(BusinessException.class)
                .hasMessage("parameter reminder_lead_hours accepts a whole number between 1 and 72");

        assertThat(valueOf(clinic, REMINDER_LEAD_HOURS).asInteger()).isEqualTo(12);
    }

    @Test
    void unsetParameterAnswersTheCatalogDefault() {
        Tenant clinic = createTenant("TEST-PARAM-DEFAULT");

        assertThat(valueOf(clinic, ROLE_MODEL).asText()).isEqualTo("SEGREGATED");
    }

    @Test
    void theNewValueGovernsTheNextOperation() {
        Tenant clinic = createTenant("TEST-PARAM-APPLY");

        inTenant(clinic, () -> parameters.change(ROLE_MODEL, ParameterValue.of("SINGLE")));

        assertThat(valueOf(clinic, ROLE_MODEL).asText()).isEqualTo("SINGLE");
    }

    @Test
    void valueOutsideTheDeclaredOptionsIsRefused() {
        Tenant clinic = createTenant("TEST-PARAM-OPTION");

        assertThatThrownBy(() -> inTenant(clinic, () -> parameters.change(ROLE_MODEL, ParameterValue.of("MIXED"))))
                .isInstanceOf(BusinessException.class)
                .hasMessage("parameter role_model accepts one of SINGLE, SEGREGATED");
    }

    @Test
    void flagParameterOnlyAcceptsTrueOrFalse() {
        Tenant clinic = createTenant("TEST-PARAM-FLAG");

        assertThatThrownBy(() -> inTenant(clinic, () -> parameters.change(BLOCK_EXPIRED_BATCH, ParameterValue.of("yes"))))
                .isInstanceOf(BusinessException.class)
                .hasMessage("parameter block_expired_batch accepts true or false");
    }

    @Test
    void unknownParameterIsNotFound() {
        Tenant clinic = createTenant("TEST-PARAM-UNKNOWN");

        assertThatThrownBy(() -> inTenant(clinic, () -> parameters.change(new ParameterCode("colour"), ParameterValue.of("blue"))))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void valueOfOneClinicDoesNotReachTheOther() {
        Tenant first = createTenant("TEST-PARAM-FIRST");
        Tenant second = createTenant("TEST-PARAM-SECOND");

        inTenant(first, () -> parameters.change(ROLE_MODEL, ParameterValue.of("SINGLE")));

        assertThat(valueOf(first, ROLE_MODEL).asText()).isEqualTo("SINGLE");
        assertThat(valueOf(second, ROLE_MODEL).asText()).isEqualTo("SEGREGATED");
    }

    private ParameterValue valueOf(Tenant clinic, ParameterCode parameter) {
        return valueInTenant(clinic, () -> port.valueOf(parameter));
    }
}
