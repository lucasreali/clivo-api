package com.example.clivoapi.core.scheduling;

import static org.hibernate.annotations.UuidGenerator.Style.VERSION_7;

import com.example.clivoapi.common.tenant.TenantScopedEntity;
import com.example.clivoapi.common.time.TimeWindow;
import com.example.clivoapi.core.practitioner.Practitioner;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "schedule_block")
public class ScheduleBlock extends TenantScopedEntity {

    @Id
    @UuidGenerator(style = VERSION_7)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "practitioner_id")
    private Practitioner practitioner;

    @Embedded
    private TimeWindow period;

    @Column(nullable = false)
    private String reason;

    protected ScheduleBlock() {
    }

    public ScheduleBlock(Practitioner practitioner, TimeWindow period, BlockReason reason) {
        this.practitioner = practitioner;
        this.period = period;
        this.reason = reason.asText();
    }

    public UUID id() {
        return id;
    }

    public boolean isClinicWide() {
        return practitioner == null;
    }

    public boolean covers(LocalDateTime moment) {
        return period.covers(moment);
    }

    public boolean appliesTo(Practitioner other) {
        return isClinicWide() || practitioner.id().equals(other.id());
    }

    public ScheduleBlockSnapshot snapshot() {
        return new ScheduleBlockSnapshot(id, practitionerId(), period, reason);
    }

    private UUID practitionerId() {
        return isClinicWide() ? null : practitioner.id();
    }
}
