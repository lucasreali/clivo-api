package com.example.clivoapi.scenarios;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.extension.ModuleCode;
import com.example.clivoapi.common.extension.ParameterCode;
import org.junit.jupiter.api.Test;

class ConfigurationScenarios extends ScenarioTest {

    private static final ModuleCode INVENTORY = new ModuleCode("inventory");

    private static final ModuleCode BATCH = new ModuleCode("batch");

    private static final ParameterCode REMINDER_LEAD_HOURS = new ParameterCode("reminder_lead_hours");

    @Test
    void ct04_aModuleIsRefusedWhileTheModuleItDependsOnIsInactive() {
        openClinic("TEST-CT04");

        assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> activate(BATCH))
                .withMessage("module batch requires module inventory to be active");

        activate(INVENTORY);
        activate(BATCH);
        assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> modules.deactivate(INVENTORY))
                .withMessage("module inventory cannot be deactivated while module batch is active");
    }

    @Test
    void ct05_aParameterOutsideItsRangeIsRefusedAndTheStandingValueRemains() {
        openClinic("TEST-CT05");
        change(REMINDER_LEAD_HOURS, "12");

        assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> change(REMINDER_LEAD_HOURS, "200"))
                .withMessage("parameter reminder_lead_hours accepts a whole number between 1 and 72");

        assertThat(parameters.valueOf(REMINDER_LEAD_HOURS).asInteger()).isEqualTo(12);
    }
}
