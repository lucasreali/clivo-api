package com.example.clivoapi.modules.inventory;

import static org.hibernate.annotations.UuidGenerator.Style.VERSION_7;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.tenant.TenantScopedEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "product")
public class Product extends TenantScopedEntity {

    @Id
    @UuidGenerator(style = VERSION_7)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Embedded
    private MeasurementUnit unit;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "min_stock", nullable = false))
    private Quantity minimum;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "on_hand", nullable = false))
    private Quantity onHand;

    @Column(name = "batch_controlled", nullable = false)
    private boolean batchControlled;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status;

    protected Product() {
    }

    public Product(ProductDetails details) {
        this.onHand = Quantity.none();
        this.status = ProductStatus.ACTIVE;
        describeAs(details);
    }

    public UUID id() {
        return id;
    }

    public String name() {
        return name;
    }

    public boolean isBelowMinimum() {
        return onHand.isLessThan(minimum);
    }

    public boolean isActive() {
        return status == ProductStatus.ACTIVE;
    }

    public boolean isBatchControlled() {
        return batchControlled;
    }

    public void increaseStock(Quantity quantity) {
        onHand = onHand.plus(quantity);
    }

    public void decreaseStock(Quantity quantity) {
        if (onHand.isLessThan(quantity)) {
            throw new BusinessException(
                    "%s has only %s %s in stock".formatted(name, onHand, unit));
        }
        onHand = onHand.minus(quantity);
    }

    public void adjustStockTo(Quantity quantity) {
        onHand = quantity;
    }

    public void describeAs(ProductDetails details) {
        this.name = details.name();
        this.unit = details.unit();
        this.minimum = details.minimum();
        this.batchControlled = details.batchControlled();
    }

    public void deactivate() {
        status = ProductStatus.INACTIVE;
    }

    public ProductSnapshot snapshot() {
        return new ProductSnapshot(id, details(), onHand, status, isBelowMinimum());
    }

    public ProductDetails details() {
        return new ProductDetails(name, unit, minimum, batchControlled);
    }
}
