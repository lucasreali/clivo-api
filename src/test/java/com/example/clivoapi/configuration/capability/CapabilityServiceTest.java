package com.example.clivoapi.configuration.capability;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.clivoapi.common.DatabaseTest;
import com.example.clivoapi.common.extension.ModuleCode;
import com.example.clivoapi.common.extension.ParameterCode;
import com.example.clivoapi.common.extension.ParameterValue;
import com.example.clivoapi.common.tenant.Tenant;
import com.example.clivoapi.core.access.Role;
import com.example.clivoapi.configuration.modules.ModuleActivationService;
import com.example.clivoapi.configuration.parameter.ClinicParameterService;
import com.example.clivoapi.configuration.parameter.EffectiveParameter;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CapabilityServiceTest extends DatabaseTest {

    private static final ModuleCode INVENTORY = new ModuleCode("inventory");
    private static final ModuleCode BATCH = new ModuleCode("batch");
    private static final ParameterCode ROLE_MODEL = new ParameterCode("role_model");

    @Autowired
    private CapabilityService capabilities;

    @Autowired
    private ModuleActivationService modules;

    @Autowired
    private ClinicParameterService parameters;

    @Test
    void clinicsConfiguredDifferentlyAnswerDifferently() {
        Tenant equipped = createTenant("TEST-CAPABILITY-FULL");
        Tenant bare = createTenant("TEST-CAPABILITY-BARE");
        inTenant(equipped, () -> modules.activate(INVENTORY));
        inTenant(equipped, () -> modules.activate(BATCH));
        inTenant(equipped, () -> parameters.change(ROLE_MODEL, ParameterValue.of("SINGLE")));

        assertThat(moduleCodesOf(equipped)).containsExactlyInAnyOrder("inventory", "batch");
        assertThat(moduleCodesOf(bare)).isEmpty();
        assertThat(valueOf(equipped, "role_model")).isEqualTo("SINGLE");
        assertThat(valueOf(bare, "role_model")).isEqualTo("SEGREGATED");
    }

    @Test
    void parameterOfAnInactiveModuleLeavesNoTrace() {
        Tenant bare = createTenant("TEST-HIDDEN-PARAM");

        assertThat(parameterCodesOf(bare))
                .contains("role_model", "reschedule_window_hours")
                .doesNotContain("block_expired_batch", "expiry_alert_days", "reminder_lead_hours");
    }

    @Test
    void activatingAModuleRevealsItsParameters() {
        Tenant clinic = createTenant("TEST-REVEALED");

        inTenant(clinic, () -> modules.activate(INVENTORY));
        inTenant(clinic, () -> modules.activate(BATCH));

        assertThat(parameterCodesOf(clinic)).contains("block_expired_batch", "expiry_alert_days");
    }

    private List<String> moduleCodesOf(Tenant clinic) {
        return capabilitiesOf(clinic).modules().stream()
                .map(definition -> definition.code().value())
                .toList();
    }

    private List<String> parameterCodesOf(Tenant clinic) {
        return capabilitiesOf(clinic).parameters().stream()
                .map(parameter -> parameter.code().value())
                .toList();
    }

    private String valueOf(Tenant clinic, String code) {
        return capabilitiesOf(clinic).parameters().stream()
                .filter(parameter -> parameter.code().value().equals(code))
                .map(EffectiveParameter::value)
                .map(ParameterValue::asText)
                .findFirst()
                .orElseThrow();
    }

    private Capabilities capabilitiesOf(Tenant clinic) {
        return valueInTenant(clinic, () -> {
            signInAs(clinic, Role.MANAGER);
            return capabilities.current();
        });
    }
}
