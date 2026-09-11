package com.example.clivoapi.modules.inventory;

import static org.hibernate.annotations.UuidGenerator.Style.VERSION_7;

import com.example.clivoapi.common.tenant.TenantScopedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "stock_movement")
public class StockMovement extends TenantScopedEntity {

    @Id
    @UuidGenerator(style = VERSION_7)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false, updatable = false)
    private Product product;

    @Column(name = "batch_id", updatable = false)
    private UUID batchId;

    @Column(name = "encounter_id", updatable = false)
    private UUID encounterId;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, updatable = false)
    private StockMovementType movementType;

    @Embedded
    private Quantity quantity;

    @Column(updatable = false)
    private String reason;

    @Column(name = "recorded_by", nullable = false, updatable = false)
    private UUID recordedBy;

    @Column(name = "recorded_at", nullable = false, updatable = false)
    private Instant recordedAt;

    protected StockMovement() {
    }

    public StockMovement(Product product, StockEntry entry, UUID recordedBy) {
        this.product = product;
        this.movementType = entry.type();
        this.quantity = entry.quantity();
        this.reason = entry.reason().asText();
        this.recordedBy = recordedBy;
        this.recordedAt = Instant.now();
    }

    private StockMovement(Product product, UUID encounterId, Quantity quantity, UUID recordedBy) {
        this.product = product;
        this.encounterId = encounterId;
        this.movementType = StockMovementType.OUTBOUND;
        this.quantity = quantity;
        this.recordedBy = recordedBy;
        this.recordedAt = Instant.now();
    }

    public static StockMovement dispensedIn(
            Product product, UUID encounterId, Quantity quantity, UUID recordedBy, UUID batchId) {
        StockMovement movement = new StockMovement(product, encounterId, quantity, recordedBy);
        movement.batchId = batchId;
        return movement;
    }

    public static StockMovement discarded(Product product, UUID batchId, StockEntry entry, UUID recordedBy) {
        StockMovement movement = new StockMovement(product, entry, recordedBy);
        movement.batchId = batchId;
        return movement;
    }

    public UUID id() {
        return id;
    }

    public boolean belongsTo(Product other) {
        return product.id().equals(other.id());
    }

    public StockMovementSnapshot snapshot() {
        return new StockMovementSnapshot(
                id, product.id(), movementType, quantity, reason, encounterId, recordedBy, recordedAt);
    }
}
