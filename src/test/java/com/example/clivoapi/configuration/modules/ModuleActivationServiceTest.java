package com.example.clivoapi.configuration.modules;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.clivoapi.common.DatabaseTest;
import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.exception.ResourceNotFoundException;
import com.example.clivoapi.common.extension.ModuleActivationState;
import com.example.clivoapi.common.extension.ModuleCode;
import com.example.clivoapi.common.tenant.Tenant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ModuleActivationServiceTest extends DatabaseTest {

    private static final ModuleCode INVENTORY = new ModuleCode("inventory");
    private static final ModuleCode BATCH = new ModuleCode("batch");

    @Autowired
    private ModuleActivationService modules;

    @Autowired
    private ModuleActivationState activationState;

    @Test
    void activatingBatchWithoutInventoryIsRefused() {
        Tenant clinic = createTenant("TEST-BATCH-ALONE");

        assertThatThrownBy(() -> inTenant(clinic, () -> modules.activate(BATCH)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("module batch requires module inventory to be active");
    }

    @Test
    void activatingBatchAfterInventoryIsAccepted() {
        Tenant clinic = createTenant("TEST-BATCH-READY");

        inTenant(clinic, () -> modules.activate(INVENTORY));
        inTenant(clinic, () -> modules.activate(BATCH));

        assertThat(activeCodesOf(clinic)).contains("inventory", "batch");
    }

    @Test
    void deactivatingInventoryWhileBatchIsActiveIsRefused() {
        Tenant clinic = createTenant("TEST-BATCH-HOLD");
        inTenant(clinic, () -> modules.activate(INVENTORY));
        inTenant(clinic, () -> modules.activate(BATCH));

        assertThatThrownBy(() -> inTenant(clinic, () -> modules.deactivate(INVENTORY)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("module inventory cannot be deactivated while module batch is active");
    }

    @Test
    void deactivatingBatchReleasesInventory() {
        Tenant clinic = createTenant("TEST-BATCH-FREE");
        inTenant(clinic, () -> modules.activate(INVENTORY));
        inTenant(clinic, () -> modules.activate(BATCH));

        inTenant(clinic, () -> modules.deactivate(BATCH));
        inTenant(clinic, () -> modules.deactivate(INVENTORY));

        assertThat(activeCodesOf(clinic)).isEmpty();
    }

    @Test
    void unknownModuleIsNotFound() {
        Tenant clinic = createTenant("TEST-UNKNOWN");

        assertThatThrownBy(() -> inTenant(clinic, () -> modules.activate(new ModuleCode("teleport"))))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void catalogReportsEveryModuleAndItsDependency() {
        Tenant clinic = createTenant("TEST-CATALOG");

        List<ModuleStatus> catalog = valueInTenant(clinic, modules::statusOfAll);

        assertThat(catalog).noneMatch(ModuleStatus::active);
        assertThat(catalog.stream().map(ModuleStatus::code).map(ModuleCode::value).toList())
                .contains(
                        "dependent",
                        "sessionpackage",
                        "inventory",
                        "batch",
                        "insurance",
                        "notification",
                        "commission");
        assertThat(catalog)
                .filteredOn(status -> status.code().equals(BATCH))
                .singleElement()
                .extracting(ModuleStatus::dependency)
                .isEqualTo(INVENTORY);
    }

    @Test
    void activationOfOneClinicDoesNotReachTheOther() {
        Tenant first = createTenant("TEST-MODULES-FIRST");
        Tenant second = createTenant("TEST-MODULES-SECOND");

        inTenant(first, () -> modules.activate(INVENTORY));

        assertThat(valueInTenant(first, () -> activationState.isActive(INVENTORY))).isTrue();
        assertThat(valueInTenant(second, () -> activationState.isActive(INVENTORY))).isFalse();
        assertThat(activeCodesOf(second)).isEmpty();
    }

    private List<String> activeCodesOf(Tenant clinic) {
        return valueInTenant(clinic, modules::activeDefinitions).stream()
                .map(definition -> definition.code().value())
                .toList();
    }
}
