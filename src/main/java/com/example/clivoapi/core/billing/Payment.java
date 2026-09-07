package com.example.clivoapi.core.billing;

import static org.hibernate.annotations.UuidGenerator.Style.VERSION_7;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.money.Money;
import com.example.clivoapi.common.tenant.TenantScopedEntity;
import jakarta.persistence.AttributeOverride;
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
@Table(name = "payment")
public class Payment extends TenantScopedEntity {

    @Id
    @UuidGenerator(style = VERSION_7)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false, updatable = false)
    private Invoice invoice;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "amount", nullable = false))
    private Money amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    @Column(name = "paid_at", nullable = false, updatable = false)
    private Instant paidAt;

    @Column(name = "recorded_by", nullable = false, updatable = false)
    private UUID recordedBy;

    @Column(name = "refunded_at")
    private Instant refundedAt;

    @Column(name = "refund_reason")
    private String refundReason;

    protected Payment() {
    }

    Payment(Invoice invoice, PaymentDetails details, UUID recordedBy) {
        this.invoice = invoice;
        this.amount = details.amount();
        this.method = details.method();
        this.paidAt = Instant.now();
        this.recordedBy = recordedBy;
    }

    public UUID id() {
        return id;
    }

    public boolean isRefunded() {
        return refundedAt != null;
    }

    public boolean identifiedBy(UUID candidate) {
        return candidate.equals(id);
    }

    Money addTo(Money total) {
        if (isRefunded()) {
            return total;
        }
        return total.plus(amount);
    }

    void refund(RefundReason reason) {
        requireNotRefunded();
        refundedAt = Instant.now();
        refundReason = reason.asText();
    }

    PaymentSnapshot snapshot() {
        return new PaymentSnapshot(id, amount, method, paidAt, refundedAt, refundReason);
    }

    private void requireNotRefunded() {
        if (!isRefunded()) {
            return;
        }
        throw new BusinessException("payment %s was already refunded".formatted(id));
    }
}
