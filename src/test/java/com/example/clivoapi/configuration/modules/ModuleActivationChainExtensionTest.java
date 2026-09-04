package com.example.clivoapi.configuration.modules;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.example.clivoapi.common.DatabaseTest;
import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.extension.ModuleActivationState;
import com.example.clivoapi.common.extension.ModuleActivationValidator;
import com.example.clivoapi.common.extension.ModuleCode;
import com.example.clivoapi.common.tenant.Tenant;
import com.example.clivoapi.patterns.chain.ExistingDataValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

class ModuleActivationChainExtensionTest extends DatabaseTest {

    private static final ModuleCode INVENTORY = new ModuleCode("inventory");
    private static final ModuleCode COMMISSION = new ModuleCode("commission");

    private static final String REFUSAL = "module commission needs a signed contract";

    @TestConfiguration
    static class SignedContractRequired {

        @Bean
        @Order(ExistingDataValidator.ORDER + 5)
        ModuleActivationValidator signedContractValidator() {
            return proposal -> {
                if (proposal.isDeactivation() || !proposal.module().equals(COMMISSION)) {
                    return;
                }
                throw new BusinessException(REFUSAL);
            };
        }
    }

    @Autowired
    private ModuleActivationService modules;

    @Autowired
    private ModuleActivationState activationState;

    @Test
    void aValidatorDeclaredOutsideTheProductionCodeJoinsTheChain() {
        Tenant clinic = createTenant("TEST-MODULE-EXT");

        assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> inTenant(clinic, () -> modules.activate(COMMISSION)))
                .withMessage(REFUSAL);
    }

    @Test
    void theRestOfTheChainKeepsWorkingAroundTheNewValidator() {
        Tenant clinic = createTenant("TEST-MODULE-EXT-OK");

        inTenant(clinic, () -> modules.activate(INVENTORY));

        assertThat(valueInTenant(clinic, () -> activationState.isActive(INVENTORY))).isTrue();
    }
}
