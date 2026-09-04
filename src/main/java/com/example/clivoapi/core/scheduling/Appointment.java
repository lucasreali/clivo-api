package com.example.clivoapi.core.scheduling;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.extension.AppointmentProposal;
import com.example.clivoapi.common.tenant.TenantScopedEntity;
import com.example.clivoapi.common.time.TimeWindow;
import com.example.clivoapi.core.catalog.Service;
import com.example.clivoapi.core.customer.Customer;
import com.example.clivoapi.core.practitioner.Practitioner;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "appointment")
@EntityListeners(AuditingEntityListener.class)
public class Appointment extends TenantScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "practitioner_id", nullable = false)
    private Practitioner practitioner;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @Embedded
    private TimeWindow period;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status;

    @Column(length = 200)
    private String reason;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    protected Appointment() {
    }

    public Appointment(Customer customer, Practitioner practitioner, Service service, TimeWindow period) {
        this.customer = customer;
        this.practitioner = practitioner;
        this.service = service;
        this.period = period;
        this.status = AppointmentStatus.SCHEDULED;
    }

    public Long id() {
        return id;
    }

    public TimeWindow period() {
        return period;
    }

    public boolean overlaps(Appointment other) {
        return other.status.occupiesAgenda() && collidesWith(other.proposal());
    }

    public boolean collidesWith(AppointmentProposal proposal) {
        return status.occupiesAgenda()
                && proposal.concerns(practitioner.id())
                && !proposal.identifies(id)
                && period.overlaps(proposal.period());
    }

    public boolean canBeRescheduled(int windowHours) {
        return status.occupiesAgenda() && period.startsAfter(LocalDateTime.now().plusHours(windowHours));
    }

    public void rescheduleTo(TimeWindow newPeriod) {
        requireOpen("rescheduled");
        period = newPeriod;
    }

    public void cancel(CancellationReason cancellation) {
        requireOpen("cancelled");
        status = AppointmentStatus.CANCELLED;
        reason = cancellation.asText();
    }

    public void checkIn() {
        requireAwaitingArrival("checked in");
        status = AppointmentStatus.ARRIVED;
    }

    public void markNoShow(CancellationReason absence) {
        requireAwaitingArrival("marked as a no-show");
        status = AppointmentStatus.NO_SHOW;
        reason = absence.asText();
    }

    public AppointmentProposal proposal() {
        return AppointmentProposal.booking(customer.id(), practitioner.id(), service.id(), period);
    }

    public AppointmentProposal proposalToStartAt(LocalDateTime start) {
        return AppointmentProposal.rescheduling(
                id,
                customer.id(),
                practitioner.id(),
                service.id(),
                TimeWindow.of(start, service.endTimeFrom(start)));
    }

    public AppointmentSnapshot snapshot() {
        return new AppointmentSnapshot(id, participants(), period, status, reason);
    }

    private AppointmentParticipants participants() {
        return new AppointmentParticipants(
                customer.id(),
                customer.name(),
                practitioner.id(),
                practitioner.name(),
                service.id(),
                service.name());
    }

    private void requireOpen(String operation) {
        if (status.occupiesAgenda()) {
            return;
        }
        refuse(operation);
    }

    private void requireAwaitingArrival(String operation) {
        if (status.awaitsArrival()) {
            return;
        }
        refuse(operation);
    }

    private void refuse(String operation) {
        throw new BusinessException("an appointment in status %s cannot be %s".formatted(status, operation));
    }
}
