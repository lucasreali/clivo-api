package com.example.clivoapi.core.billing;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.money.Money;
import com.example.clivoapi.common.tenant.TenantScopedEntity;
import com.example.clivoapi.core.catalog.Service;
import com.example.clivoapi.core.customer.Customer;
import com.example.clivoapi.core.encounter.Encounter;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoice")
public class Invoice extends TenantScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "encounter_id", nullable = false, updatable = false)
    private Encounter encounter;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false, updatable = false)
    private Customer customer;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "gross_amount", nullable = false))
    private Money grossAmount;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "discount", nullable = false))
    private Money discount;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "net_amount", nullable = false))
    private Money netAmount;

    @Column(name = "discount_reason")
    private String discountReason;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceCoverage coverage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("paidAt")
    private List<Payment> payments = new ArrayList<>();

    protected Invoice() {
    }

    public Invoice(Encounter encounter, Customer customer, Service service, LocalDate dueDate) {
        this.encounter = encounter;
        this.customer = customer;
        this.grossAmount = service.price();
        this.discount = Money.zero();
        this.netAmount = service.price();
        this.dueDate = dueDate;
        this.status = InvoiceStatus.OPEN;
        this.coverage = InvoiceCoverage.DIRECT;
        this.createdAt = Instant.now();
        this.items.add(new InvoiceItem(this, service));
    }

    public Long id() {
        return id;
    }

    public Money calculateNetAmount() {
        return grossAmount.minus(discount);
    }

    public Money outstandingBalance() {
        return netAmount.minus(paidTotal());
    }

    public boolean isOverdue() {
        return !outstandingBalance().isZero() && dueDate.isBefore(LocalDate.now());
    }

    public void applyDiscount(Money amount, DiscountReason reason) {
        requireOpen("discounted");
        requireWithinGross(amount);
        discount = amount;
        discountReason = reason.asText();
        netAmount = calculateNetAmount();
        refreshStatus();
    }

    public void coverBy(InvoiceCoverage source) {
        requireOpen("covered");
        coverage = source;
        discount = grossAmount;
        discountReason = "fully covered by %s".formatted(source);
        netAmount = Money.zero();
        refreshStatus();
    }

    public void settle(PaymentDetails details, Long recordedBy) {
        requireOpen("paid");
        requireWithinOutstanding(details.amount());
        payments.add(new Payment(this, details, recordedBy));
        refreshStatus();
    }

    public void refund(Long paymentId, RefundReason reason) {
        paymentNumbered(paymentId).refund(reason);
        refreshStatus();
    }

    public InvoiceSnapshot snapshot() {
        return new InvoiceSnapshot(
                id,
                encounter.id(),
                customer.id(),
                customer.name(),
                new InvoiceAmounts(grossAmount, discount, netAmount, outstandingBalance()),
                discountReason,
                dueDate,
                status,
                coverage,
                isOverdue(),
                lines(),
                receipts());
    }

    private List<InvoiceLine> lines() {
        return items.stream().map(InvoiceItem::line).toList();
    }

    private List<PaymentSnapshot> receipts() {
        return payments.stream().map(Payment::snapshot).toList();
    }

    private Money paidTotal() {
        return payments.stream().reduce(Money.zero(), (total, payment) -> payment.addTo(total), Money::plus);
    }

    private Payment paymentNumbered(Long paymentId) {
        return payments.stream()
                .filter(payment -> payment.identifiedBy(paymentId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        "payment %d does not belong to invoice %d".formatted(paymentId, id)));
    }

    private void refreshStatus() {
        status = statusFor(outstandingBalance());
    }

    private InvoiceStatus statusFor(Money outstanding) {
        if (outstanding.isZero()) {
            return InvoiceStatus.PAID;
        }
        if (outstanding.equals(netAmount)) {
            return InvoiceStatus.OPEN;
        }
        return InvoiceStatus.PARTIAL;
    }

    private void requireWithinOutstanding(Money amount) {
        Money outstanding = outstandingBalance();
        if (!amount.isGreaterThan(outstanding)) {
            return;
        }
        throw new BusinessException(
                "a payment of %s is above the outstanding balance of %s".formatted(amount, outstanding));
    }

    private void requireWithinGross(Money amount) {
        if (!amount.isGreaterThan(grossAmount)) {
            return;
        }
        throw new BusinessException(
                "a discount of %s is above the invoiced amount of %s".formatted(amount, grossAmount));
    }

    private void requireOpen(String operation) {
        if (status.acceptsChange()) {
            return;
        }
        throw new BusinessException("an invoice in status %s cannot be %s".formatted(status, operation));
    }
}
