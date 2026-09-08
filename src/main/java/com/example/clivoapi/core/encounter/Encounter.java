package com.example.clivoapi.core.encounter;

import static org.hibernate.annotations.UuidGenerator.Style.VERSION_7;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.extension.CompletedEncounter;
import com.example.clivoapi.common.extension.RecordFilling;
import com.example.clivoapi.common.extension.RecordSheet;
import com.example.clivoapi.common.extension.RecordValues;
import com.example.clivoapi.common.extension.SuppliesUsed;
import com.example.clivoapi.common.tenant.TenantScopedEntity;
import com.example.clivoapi.core.access.AppUser;
import com.example.clivoapi.core.catalog.Service;
import com.example.clivoapi.core.customer.Customer;
import com.example.clivoapi.core.practitioner.Practitioner;
import com.example.clivoapi.core.scheduling.Appointment;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "encounter")
public class Encounter extends TenantScopedEntity {

    @Id
    @UuidGenerator(style = VERSION_7)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "practitioner_id", nullable = false)
    private Practitioner practitioner;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @Column(name = "record_template_id", nullable = false, updatable = false)
    private UUID recordTemplateId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "field_values", nullable = false)
    private Map<String, Object> fieldValues;

    @Column(name = "started_at", nullable = false, updatable = false)
    private Instant startedAt;

    @Column(name = "last_saved_at")
    private Instant lastSavedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "signed_by")
    private AppUser signedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EncounterStatus status;

    protected Encounter() {
    }

    public Encounter(Customer customer, Practitioner practitioner, Service service, UUID recordTemplateId) {
        this.customer = customer;
        this.practitioner = practitioner;
        this.service = service;
        this.recordTemplateId = recordTemplateId;
        this.fieldValues = Map.of();
        this.startedAt = Instant.now();
        this.status = EncounterStatus.DRAFT;
    }

    public Encounter(
            Appointment appointment,
            Customer customer,
            Practitioner practitioner,
            Service service,
            UUID recordTemplateId) {
        this(customer, practitioner, service, recordTemplateId);
        this.appointment = appointment;
    }

    public UUID id() {
        return id;
    }

    public UUID customerId() {
        return customer.id();
    }

    public String practitionerName() {
        return practitioner.name();
    }

    public Instant recordedAt() {
        return Optional.ofNullable(completedAt).orElse(startedAt);
    }

    public RecordFilling filling() {
        return new RecordFilling(recordTemplateId, RecordValues.of(fieldValues));
    }

    public void fill(RecordValues values) {
        requireOpen("filled in");
        fieldValues = values.asMap();
        lastSavedAt = Instant.now();
    }

    public void complete(AppUser signer) {
        requireOpen("completed");
        status = EncounterStatus.COMPLETED;
        completedAt = Instant.now();
        signedBy = signer;
    }

    public boolean isCompleted() {
        return status == EncounterStatus.COMPLETED;
    }

    public SuppliesUsed consume(UUID productId, BigDecimal quantity) {
        requireOpen("supplied");
        return new SuppliesUsed(id, productId, quantity);
    }

    public CompletedEncounter completion() {
        return new CompletedEncounter(id, customer.id(), practitioner.id(), service.id());
    }

    public EncounterSnapshot snapshotWith(RecordSheet sheet, ClinicalContext context) {
        return new EncounterSnapshot(id, participants(), context, sheet, timing(), signature(), status);
    }

    public EncounterSnapshot summary() {
        return snapshotWith(null, ClinicalContext.undisclosed());
    }

    private EncounterParticipants participants() {
        return new EncounterParticipants(
                appointmentId().orElse(null),
                new AttendedCustomer(customer.id(), customer.name(), customer.details().birthDate()),
                new AttendingPractitioner(
                        practitioner.id(), practitioner.name(), practitioner.snapshot().details().licenseNumber()),
                new ProvidedService(service.id(), service.name()));
    }

    private EncounterTiming timing() {
        return new EncounterTiming(startedAt, lastSavedAt, completedAt);
    }

    private Signature signature() {
        return Optional.ofNullable(signedBy)
                .map(signer -> new Signature(signer.id(), signer.name()))
                .orElse(null);
    }

    private Optional<UUID> appointmentId() {
        return Optional.ofNullable(appointment).map(Appointment::id);
    }

    private void requireOpen(String operation) {
        if (status.isOpen()) {
            return;
        }
        throw new BusinessException("an encounter in status %s cannot be %s".formatted(status, operation));
    }
}
