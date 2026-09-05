package com.example.clivoapi.core.encounter;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.extension.RecordFilling;
import com.example.clivoapi.common.extension.RecordValues;
import com.example.clivoapi.common.tenant.TenantScopedEntity;
import com.example.clivoapi.core.customer.Customer;
import com.example.clivoapi.core.practitioner.Practitioner;
import com.example.clivoapi.core.scheduling.Appointment;
import com.example.clivoapi.core.scheduling.AppointmentParticipants;
import com.example.clivoapi.core.scheduling.AppointmentSnapshot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "encounter")
public class Encounter extends TenantScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "practitioner_id", nullable = false)
    private Practitioner practitioner;

    @Column(name = "record_template_id", nullable = false, updatable = false)
    private Long recordTemplateId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "field_values", nullable = false)
    private Map<String, Object> fieldValues;

    @Column(name = "started_at", nullable = false, updatable = false)
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EncounterStatus status;

    protected Encounter() {
    }

    public Encounter(Customer customer, Practitioner practitioner, Long recordTemplateId) {
        this.customer = customer;
        this.practitioner = practitioner;
        this.recordTemplateId = recordTemplateId;
        this.fieldValues = Map.of();
        this.startedAt = Instant.now();
        this.status = EncounterStatus.DRAFT;
    }

    public Encounter(Appointment appointment, Customer customer, Practitioner practitioner, Long recordTemplateId) {
        this(customer, practitioner, recordTemplateId);
        this.appointment = appointment;
    }

    public Long id() {
        return id;
    }

    public RecordFilling filling() {
        return new RecordFilling(recordTemplateId, RecordValues.of(fieldValues));
    }

    public void fill(RecordValues values) {
        requireOpen("filled in");
        fieldValues = values.asMap();
    }

    public void complete() {
        requireOpen("completed");
        status = EncounterStatus.COMPLETED;
        completedAt = Instant.now();
    }

    public boolean isCompleted() {
        return status == EncounterStatus.COMPLETED;
    }

    public EncounterSnapshot snapshot() {
        return new EncounterSnapshot(id, participants(), filling(), startedAt, completedAt, status);
    }

    private EncounterParticipants participants() {
        return new EncounterParticipants(
                appointmentId().orElse(null),
                customer.id(),
                customer.name(),
                practitioner.id(),
                practitioner.name(),
                booking().map(AppointmentParticipants::serviceId).orElse(null),
                booking().map(AppointmentParticipants::serviceName).orElse(null));
    }

    private Optional<Long> appointmentId() {
        return booked().map(Appointment::id);
    }

    private Optional<AppointmentParticipants> booking() {
        return booked().map(Appointment::snapshot).map(AppointmentSnapshot::participants);
    }

    private Optional<Appointment> booked() {
        return Optional.ofNullable(appointment);
    }

    private void requireOpen(String operation) {
        if (status.isOpen()) {
            return;
        }
        throw new BusinessException("an encounter in status %s cannot be %s".formatted(status, operation));
    }
}
