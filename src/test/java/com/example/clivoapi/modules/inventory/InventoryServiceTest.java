package com.example.clivoapi.modules.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.clivoapi.common.exception.BusinessException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class InventoryServiceTest extends InventoryFixture {

    @Test
    void anInboundMovementRaisesTheStockOnHand() {
        openClinicWithInventory("TEST-INV-INBOUND");
        UUID gauze = registerGauze("20");

        ProductSnapshot product = inventory.move(gauze, inboundOf("50", "compra mensal"));

        assertThat(product.onHand()).isEqualTo(Quantity.of("50"));
        assertThat(product.belowMinimum()).isFalse();
    }

    @Test
    void aProductUnderItsMinimumIsFlagged() {
        openClinicWithInventory("TEST-INV-MINIMUM");
        UUID gauze = registerGauze("20");
        inventory.move(gauze, inboundOf("15", "compra parcial"));

        assertThat(inventory.findOne(gauze).belowMinimum()).isTrue();
        assertThat(inventory.belowMinimum()).extracting(ProductSnapshot::id).containsExactly(gauze);
    }

    @Test
    void anOutboundMovementBeyondTheStockOnHandIsRefused() {
        openClinicWithInventory("TEST-INV-SHORTAGE");
        UUID gauze = registerGauze("0");
        inventory.move(gauze, inboundOf("5", "compra inicial"));

        assertThatThrownBy(() -> inventory.move(gauze, outboundOf("9", "uso interno")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Sterile gauze has only 5.00 un in stock");
    }

    @Test
    void anAdjustmentReplacesTheStockOnHandAfterACount() {
        openClinicWithInventory("TEST-INV-ADJUST");
        UUID gauze = registerGauze("0");
        inventory.move(gauze, inboundOf("40", "compra mensal"));

        ProductSnapshot product = inventory.move(
                gauze,
                new StockEntry(StockMovementType.ADJUSTMENT, Quantity.of("36"), new MovementReason("contagem")));

        assertThat(product.onHand()).isEqualTo(Quantity.of("36"));
    }

    @Test
    void everyMovementKeepsItsAuthorAndItsReason() {
        openClinicWithInventory("TEST-INV-AUTHOR");
        UUID gauze = registerGauze("0");
        inventory.move(gauze, inboundOf("10", "compra mensal"));

        List<StockMovementSnapshot> history = inventory.historyOf(gauze);

        assertThat(history).hasSize(1);
        assertThat(history.getFirst().recordedBy()).isNotNull();
        assertThat(history.getFirst().statedReason()).contains("compra mensal");
    }

    @Test
    void aMovementWithoutReasonIsRefused() {
        assertThatThrownBy(() -> new MovementReason(" "))
                .isInstanceOf(BusinessException.class)
                .hasMessage("a stock movement is recorded with its reason");
    }

    @Test
    void anEncounterTakesTheSuppliesItUsedOutOfStock() {
        openClinicWithInventory("TEST-INV-DISPENSE");
        UUID gauze = registerGauze("0");
        inventory.move(gauze, inboundOf("30", "compra mensal"));
        UUID encounterId = anOpenEncounter();

        encounters.useSupplies(encounterId, gauze, Quantity.of("4").amount());

        assertThat(inventory.findOne(gauze).onHand()).isEqualTo(Quantity.of("26"));
        assertThat(inventory.historyOf(gauze).getFirst().encounter()).contains(encounterId);
    }

    @Test
    void aClinicWithoutTheModuleNeverTouchesItsStock() {
        openClinic("TEST-INV-OFF");
        UUID gauze = registerGauze("0");
        inventory.move(gauze, inboundOf("30", "compra mensal"));
        UUID encounterId = anOpenEncounter();

        encounters.useSupplies(encounterId, gauze, Quantity.of("4").amount());

        assertThat(inventory.findOne(gauze).onHand()).isEqualTo(Quantity.of("30"));
    }

    private StockEntry inboundOf(String quantity, String reason) {
        return new StockEntry(StockMovementType.INBOUND, Quantity.of(quantity), new MovementReason(reason));
    }

    private StockEntry outboundOf(String quantity, String reason) {
        return new StockEntry(StockMovementType.OUTBOUND, Quantity.of(quantity), new MovementReason(reason));
    }
}
