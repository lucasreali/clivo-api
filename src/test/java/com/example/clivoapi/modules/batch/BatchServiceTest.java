package com.example.clivoapi.modules.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.extension.ModuleCode;
import com.example.clivoapi.modules.inventory.InventoryFixture;
import com.example.clivoapi.modules.inventory.MeasurementUnit;
import com.example.clivoapi.modules.inventory.MovementReason;
import com.example.clivoapi.modules.inventory.ProductDetails;
import com.example.clivoapi.modules.inventory.Quantity;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class BatchServiceTest extends InventoryFixture {

    private static final ModuleCode BATCH = new ModuleCode("batch");

    @Autowired
    private BatchService batches;

    @Test
    void aReceivedBatchRaisesTheStockOfItsProduct() {
        openClinicWithBatches("TEST-BATCH-RECEIVE");
        Long anaesthetic = registerAnaesthetic();

        BatchSnapshot batch = batches.receive(anaesthetic, batchOf("L-2201", LocalDate.now().plusMonths(6), "50"));

        assertThat(batch.status()).isEqualTo(BatchStatus.AVAILABLE);
        assertThat(batch.expired()).isFalse();
        assertThat(inventory.findOne(anaesthetic).onHand()).isEqualTo(Quantity.of("50"));
    }

    @Test
    void aProductThatIsNotControlledByBatchDoesNotReceiveOne() {
        openClinicWithBatches("TEST-BATCH-PLAIN");
        Long gauze = registerGauze("0");

        assertThatThrownBy(() -> batches.receive(gauze, batchOf("L-1", LocalDate.now().plusMonths(2), "10")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Gaze estéril is not controlled by batch");
    }

    @Test
    void aBatchKnowsWhenItExpiredAndWhenItIsAboutTo() {
        openClinicWithBatches("TEST-BATCH-EXPIRY");
        Long anaesthetic = registerAnaesthetic();
        batches.receive(anaesthetic, batchOf("L-OLD", LocalDate.now().minusDays(1), "10"));
        batches.receive(anaesthetic, batchOf("L-SOON", LocalDate.now().plusDays(20), "10"));
        batches.receive(anaesthetic, batchOf("L-FAR", LocalDate.now().plusYears(2), "10"));

        assertThat(batches.awaitingDiscard()).extracting(BatchSnapshot::expired).containsExactly(true);
        assertThat(batches.expiringWithin(null)).hasSize(2);
        assertThat(batches.expiringWithin(10)).hasSize(1);
    }

    @Test
    void anExpiredBatchStaysVisibleForTheDiscardReviewUntilItIsDiscarded() {
        openClinicWithBatches("TEST-BATCH-DISCARD");
        Long anaesthetic = registerAnaesthetic();
        BatchSnapshot expired = batches.receive(anaesthetic, batchOf("L-OLD", LocalDate.now().minusDays(3), "8"));

        assertThat(batches.awaitingDiscard()).hasSize(1);

        BatchSnapshot discarded = batches.discard(expired.id(), new MovementReason("vencido na conferência"));

        assertThat(discarded.status()).isEqualTo(BatchStatus.DISCARDED);
        assertThat(batches.awaitingDiscard()).isEmpty();
        assertThat(inventory.findOne(anaesthetic).onHand()).isEqualTo(Quantity.none());
    }

    @Test
    void aBatchAlreadyDiscardedIsNotDiscardedTwice() {
        openClinicWithBatches("TEST-BATCH-TWICE");
        Long anaesthetic = registerAnaesthetic();
        BatchSnapshot batch = batches.receive(anaesthetic, batchOf("L-OLD", LocalDate.now().minusDays(3), "8"));
        batches.discard(batch.id(), new MovementReason("vencido"));

        assertThatThrownBy(() -> batches.discard(batch.id(), new MovementReason("vencido")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("batch L-OLD was already discarded");
    }

    @Test
    void aBatchOnlyHandsOutWhatItStillHolds() {
        openClinicWithBatches("TEST-BATCH-AVAILABLE");
        Long anaesthetic = registerAnaesthetic();
        BatchSnapshot received = batches.receive(anaesthetic, batchOf("L-2201", LocalDate.now().plusMonths(6), "5"));
        Batch batch = batches.usableFor(anaesthetic).getFirst();

        assertThat(batch.id()).isEqualTo(received.id());
        assertThat(batch.hasAvailable(Quantity.of("5"))).isTrue();
        assertThat(batch.hasAvailable(Quantity.of("6"))).isFalse();
        assertThat(batch.expiresWithin(365)).isTrue();
        assertThat(batch.isExpired(LocalDate.now())).isFalse();
    }

    private void openClinicWithBatches(String code) {
        openClinicWithInventory(code);
        activate(BATCH);
    }

    private Long registerAnaesthetic() {
        return registerProduct(new ProductDetails("Anestésico", new MeasurementUnit("ml"), Quantity.none(), true));
    }

    private BatchDetails batchOf(String code, LocalDate expiresOn, String quantity) {
        return new BatchDetails(new BatchCode(code), expiresOn, Quantity.of(quantity), "Lab Alpha");
    }
}
