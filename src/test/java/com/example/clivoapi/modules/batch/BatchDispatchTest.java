package com.example.clivoapi.modules.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.extension.ModuleCode;
import com.example.clivoapi.common.extension.ParameterCode;
import com.example.clivoapi.common.extension.ParameterValue;
import com.example.clivoapi.configuration.parameter.ClinicParameterService;
import com.example.clivoapi.modules.inventory.InventoryFixture;
import com.example.clivoapi.modules.inventory.MeasurementUnit;
import com.example.clivoapi.modules.inventory.MovementReason;
import com.example.clivoapi.modules.inventory.ProductDetails;
import com.example.clivoapi.modules.inventory.Quantity;
import com.example.clivoapi.modules.inventory.StockEntry;
import com.example.clivoapi.modules.inventory.StockMovementType;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class BatchDispatchTest extends InventoryFixture {

    private static final ModuleCode BATCH = new ModuleCode("batch");

    private static final ParameterCode BLOCK_EXPIRED_BATCH = new ParameterCode("block_expired_batch");

    @Autowired
    private BatchService batches;

    @Autowired
    private ClinicParameterService parameters;

    @Test
    void anEncounterTakesItsSuppliesFromABatchTheClinicPolicyAccepts() {
        openClinicWithBatches("TEST-DISPATCH-FRESH", "true");
        UUID anaesthetic = registerAnaesthetic();
        BatchSnapshot expired = batches.receive(anaesthetic, batchOf("L-OLD", LocalDate.now().minusDays(2), "10"));
        BatchSnapshot fresh = batches.receive(anaesthetic, batchOf("L-NEW", LocalDate.now().plusMonths(8), "10"));

        encounters.useSupplies(anOpenEncounter(), anaesthetic, Quantity.of("3").amount());

        assertThat(batches.findOne(fresh.id()).details().quantity()).isEqualTo(Quantity.of("7"));
        assertThat(batches.findOne(expired.id()).details().quantity()).isEqualTo(Quantity.of("10"));
        assertThat(inventory.findOne(anaesthetic).onHand()).isEqualTo(Quantity.of("17"));
    }

    @Test
    void aClinicThatBlocksExpiredBatchesRefusesTheDispenseAndKeepsItsStock() {
        openClinicWithBatches("TEST-DISPATCH-BLOCK", "true");
        UUID anaesthetic = registerAnaesthetic();
        batches.receive(anaesthetic, batchOf("L-OLD", LocalDate.now().minusDays(2), "10"));
        UUID encounterId = anOpenEncounter();

        assertThatThrownBy(() -> encounters.useSupplies(encounterId, anaesthetic, Quantity.of("3").amount()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("no batch of Anestésico is within its expiry date");

        assertThat(inventory.findOne(anaesthetic).onHand()).isEqualTo(Quantity.of("10"));
    }

    @Test
    void aClinicThatOnlyWarnsSpendsTheExpiredBatchRatherThanRefusing() {
        openClinicWithBatches("TEST-DISPATCH-WARN", "false");
        UUID anaesthetic = registerAnaesthetic();
        BatchSnapshot expired = batches.receive(anaesthetic, batchOf("L-OLD", LocalDate.now().minusDays(2), "10"));

        encounters.useSupplies(anOpenEncounter(), anaesthetic, Quantity.of("3").amount());

        assertThat(batches.findOne(expired.id()).details().quantity()).isEqualTo(Quantity.of("7"));
        assertThat(inventory.findOne(anaesthetic).onHand()).isEqualTo(Quantity.of("7"));
    }

    @Test
    void aProductWithoutBatchControlIsDispensedTheSameWayAsBefore() {
        openClinicWithBatches("TEST-DISPATCH-PLAIN", "true");
        UUID gauze = registerGauze("0");
        inventory.move(gauze, inboundOf("10"));

        encounters.useSupplies(anOpenEncounter(), gauze, Quantity.of("4").amount());

        assertThat(inventory.findOne(gauze).onHand()).isEqualTo(Quantity.of("6"));
    }

    private void openClinicWithBatches(String code, String blockExpiredBatch) {
        openClinicWithInventory(code);
        activate(BATCH);
        parameters.change(BLOCK_EXPIRED_BATCH, ParameterValue.of(blockExpiredBatch));
    }

    private StockEntry inboundOf(String quantity) {
        return new StockEntry(
                StockMovementType.INBOUND, Quantity.of(quantity), new MovementReason("compra inicial"));
    }

    private UUID registerAnaesthetic() {
        return registerProduct(new ProductDetails("Anestésico", new MeasurementUnit("ml"), Quantity.none(), true));
    }

    private BatchDetails batchOf(String code, LocalDate expiresOn, String quantity) {
        return new BatchDetails(new BatchCode(code), expiresOn, Quantity.of(quantity), "Lab Alpha");
    }
}
