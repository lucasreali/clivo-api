package com.example.clivoapi.core.billing;

import com.example.clivoapi.common.money.Money;
import com.example.clivoapi.core.catalog.Service;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "invoice_item")
public class InvoiceItem {

    private static final BigDecimal SINGLE = BigDecimal.ONE;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private Service service;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private BigDecimal quantity;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "unit_price", nullable = false))
    private Money unitPrice;

    protected InvoiceItem() {
    }

    InvoiceItem(Invoice invoice, Service service) {
        this.invoice = invoice;
        this.service = service;
        this.description = service.name();
        this.quantity = SINGLE;
        this.unitPrice = service.price();
    }

    InvoiceLine line() {
        return new InvoiceLine(service.id(), description, quantity, unitPrice);
    }
}
