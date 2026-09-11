package com.example.clivoapi.scenarios;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.extension.BatchChoice;
import com.example.clivoapi.common.extension.ModuleCode;
import com.example.clivoapi.common.extension.ParameterCode;
import com.example.clivoapi.modules.batch.BatchCode;
import com.example.clivoapi.modules.batch.BatchDetails;
import com.example.clivoapi.modules.batch.BatchService;
import com.example.clivoapi.modules.inventory.InventoryService;
import com.example.clivoapi.modules.inventory.MeasurementUnit;
import com.example.clivoapi.modules.inventory.ProductDetails;
import com.example.clivoapi.modules.inventory.Quantity;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class BatchScenarios extends ScenarioTest {

    private static final ModuleCode INVENTORY = new ModuleCode("inventory");

    private static final ModuleCode BATCH = new ModuleCode("batch");

    private static final ParameterCode BLOCK_EXPIRED_BATCH = new ParameterCode("block_expired_batch");

    private static final Quantity ONE_DOSE = Quantity.of("1");

    @Autowired
    private InventoryService inventory;

    @Autowired
    private BatchService batches;

    @Test
    void ct13_aClinicThatBlocksExpiredBatchesRefusesTheSelection() {
        UUID anaesthetic = openClinicWithAnExpiredBatch("TEST-CT13", "true");

        assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> batches.selectFor(anaesthetic, ONE_DOSE))
                .withMessage("no batch of Anaesthetic is within its expiry date");
    }

    @Test
    void ct14_aClinicThatOnlyWarnsSelectsTheExpiredBatchWithAnAlert() {
        UUID anaesthetic = openClinicWithAnExpiredBatch("TEST-CT14", "false");

        BatchChoice choice = batches.selectFor(anaesthetic, ONE_DOSE);

        assertThat(choice.code()).isEqualTo("L-OLD");
        assertThat(choice.alert()).isPresent();
    }

    private UUID openClinicWithAnExpiredBatch(String code, String blockExpiredBatch) {
        openClinic(code);
        activate(INVENTORY);
        activate(BATCH);
        change(BLOCK_EXPIRED_BATCH, blockExpiredBatch);
        UUID anaesthetic = inventory
                .register(new ProductDetails("Anaesthetic", new MeasurementUnit("ml"), Quantity.none(), true))
                .id();
        batches.receive(
                anaesthetic,
                new BatchDetails(
                        new BatchCode("L-OLD"), LocalDate.now().minusDays(2), Quantity.of("10"), "Lab Alpha"));
        return anaesthetic;
    }
}
