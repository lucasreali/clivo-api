package com.example.clivoapi.core.clinical;

import static org.hibernate.annotations.UuidGenerator.Style.VERSION_7;

import com.example.clivoapi.common.tenant.TenantScopedEntity;
import com.example.clivoapi.core.access.AppUser;
import com.example.clivoapi.core.customer.Customer;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "clinical_alert")
public class ClinicalAlert extends TenantScopedEntity {

    @Id
    @UuidGenerator(style = VERSION_7)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false, updatable = false)
    private Customer customer;

    @Embedded
    private AlertNote note;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recorded_by", nullable = false)
    private AppUser author;

    protected ClinicalAlert() {
    }

    public ClinicalAlert(Customer customer, AlertNote note, AppUser author) {
        this.customer = customer;
        rewriteAs(note, author);
    }

    public UUID id() {
        return id;
    }

    public void rewriteAs(AlertNote note, AppUser author) {
        this.note = note;
        this.author = author;
        this.recordedAt = Instant.now();
    }

    public ClinicalAlertSnapshot snapshot() {
        return new ClinicalAlertSnapshot(
                id, customer.id(), note.asText(), author.id(), author.name(), recordedAt);
    }
}
