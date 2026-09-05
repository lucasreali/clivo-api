package com.example.clivoapi.modules.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.extension.BatchChoice;
import com.example.clivoapi.common.extension.ModuleCode;
import com.example.clivoapi.common.extension.ParameterCode;
import com.example.clivoapi.common.extension.ParameterValue;
import com.example.clivoapi.common.tenant.Tenant;
import com.example.clivoapi.configuration.parameter.ClinicParameterService;
import com.example.clivoapi.modules.inventory.InventoryFixture;
import com.example.clivoapi.modules.inventory.MeasurementUnit;
import com.example.clivoapi.modules.inventory.ProductDetails;
import com.example.clivoapi.modules.inventory.Quantity;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ExpiredBatchPolicyTest extends InventoryFixture {

    private static final ModuleCode BATCH = new ModuleCode("batch");
    private static final ParameterCode BLOCK_EXPIRED_BATCH = new ParameterCode("block_expired_batch");
    private static final Quantity ONE_DOSE = Quantity.of("1");

    @Autowired
    private BatchService batches;

    @Autowired
    private ClinicParameterService parameters;

    @Test
    void aClinicThatBlocksExpiredBatchesRefusesTheSelection() {
        openClinicWithBatches("TEST-POLICY-BLOCK", "true");
        Long anaesthetic = onlyAnExpiredBatch();

        assertThatThrownBy(() -> batches.selectFor(anaesthetic, ONE_DOSE))
                .isInstanceOf(BusinessException.class)
                .hasMessage("no batch of Anestésico is within its expiry date");
    }

    @Test
    void aClinicThatOnlyWarnsSelectsTheExpiredBatchWithAnAlert() {
        openClinicWithBatches("TEST-POLICY-WARN", "false");
        Long anaesthetic = onlyAnExpiredBatch();

        BatchChoice choice = batches.selectFor(anaesthetic, ONE_DOSE);

        assertThat(choice.code()).isEqualTo("L-OLD");
        assertThat(choice.alert()).contains("batch L-OLD expired on %s".formatted(LocalDate.now().minusDays(2)));
    }

    @Test
    void aFreshBatchIsChosenWithoutAnyAlertUnderEitherPolicy() {
        openClinicWithBatches("TEST-POLICY-FRESH", "true");
        Long anaesthetic = onlyAnExpiredBatch();
        batches.receive(anaesthetic, batchOf("L-NEW", LocalDate.now().plusMonths(8)));

        BatchChoice choice = batches.selectFor(anaesthetic, ONE_DOSE);

        assertThat(choice.code()).isEqualTo("L-NEW");
        assertThat(choice.alert()).isEmpty();
    }

    @Test
    void theSameClinicChangesItsMindWithOneParameterRow() {
        openClinicWithBatches("TEST-POLICY-FLIP", "true");
        Long anaesthetic = onlyAnExpiredBatch();

        assertThatThrownBy(() -> batches.selectFor(anaesthetic, ONE_DOSE)).isInstanceOf(BusinessException.class);

        parameters.change(BLOCK_EXPIRED_BATCH, ParameterValue.of("false"));

        assertThat(batches.selectFor(anaesthetic, ONE_DOSE).alert()).isPresent();
    }

    private Tenant openClinicWithBatches(String code, String blockExpiredBatch) {
        Tenant clinic = openClinicWithInventory(code);
        activate(BATCH);
        parameters.change(BLOCK_EXPIRED_BATCH, ParameterValue.of(blockExpiredBatch));
        return clinic;
    }

    private Long onlyAnExpiredBatch() {
        Long anaesthetic =
                registerProduct(new ProductDetails("Anestésico", new MeasurementUnit("ml"), Quantity.none(), true));
        batches.receive(anaesthetic, batchOf("L-OLD", LocalDate.now().minusDays(2)));
        return anaesthetic;
    }

    private BatchDetails batchOf(String code, LocalDate expiresOn) {
        return new BatchDetails(new BatchCode(code), expiresOn, Quantity.of("10"), "Lab Alpha");
    }
}
