package com.example.clivoapi.modules.batch;

import static org.hibernate.annotations.UuidGenerator.Style.VERSION_7;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.extension.BatchCandidate;
import com.example.clivoapi.common.tenant.TenantScopedEntity;
import com.example.clivoapi.modules.inventory.Product;
import com.example.clivoapi.modules.inventory.Quantity;
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
import java.time.LocalDate;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "batch")
public class Batch extends TenantScopedEntity {

    @Id
    @UuidGenerator(style = VERSION_7)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false, updatable = false)
    private Product product;

    @Embedded
    private BatchCode code;

    @Column(name = "expires_on", nullable = false)
    private LocalDate expiresOn;

    @Embedded
    private Quantity quantity;

    private String manufacturer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BatchStatus status;

    protected Batch() {
    }

    public Batch(Product product, BatchDetails details) {
        this.product = product;
        this.status = BatchStatus.AVAILABLE;
        this.code = details.code();
        this.expiresOn = details.expiresOn();
        this.quantity = details.quantity();
        this.manufacturer = details.manufacturer();
    }

    public UUID id() {
        return id;
    }

    public Product product() {
        return product;
    }

    public Quantity quantity() {
        return quantity;
    }

    public boolean isExpired(LocalDate reference) {
        return !expiresOn.isAfter(reference);
    }

    public boolean expiresWithin(int days) {
        return isExpired(LocalDate.now().plusDays(days));
    }

    public boolean hasAvailable(Quantity wanted) {
        return isAvailable() && !quantity.isLessThan(wanted);
    }

    public boolean isAvailable() {
        return status == BatchStatus.AVAILABLE;
    }

    public void take(Quantity wanted) {
        if (!hasAvailable(wanted)) {
            throw new BusinessException("batch %s has only %s left".formatted(code, quantity));
        }
        quantity = quantity.minus(wanted);
    }

    public void discard() {
        if (status == BatchStatus.DISCARDED) {
            throw new BusinessException("batch %s was already discarded".formatted(code));
        }
        status = BatchStatus.DISCARDED;
    }

    public BatchCandidate asCandidate() {
        return new BatchCandidate(id, code.asText(), expiresOn, isExpired(LocalDate.now()));
    }

    public BatchSnapshot snapshot() {
        return new BatchSnapshot(id, product.id(), product.name(), details(), status, isExpired(LocalDate.now()));
    }

    public BatchDetails details() {
        return new BatchDetails(code, expiresOn, quantity, manufacturer);
    }
}
