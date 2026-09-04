package com.example.clivoapi.core.practitioner;

import com.example.clivoapi.common.tenant.TenantScopedEntity;
import com.example.clivoapi.common.time.TimeWindow;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "practitioner")
public class Practitioner extends TenantScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "license_number")
    private String licenseNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PractitionerStatus status;

    @OneToMany(mappedBy = "practitioner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AvailabilitySlot> availability = new ArrayList<>();

    protected Practitioner() {
    }

    public Practitioner(PractitionerDetails details) {
        this.status = PractitionerStatus.ACTIVE;
        describeAs(details);
    }

    public Long id() {
        return id;
    }

    public boolean isActive() {
        return status == PractitionerStatus.ACTIVE;
    }

    public String name() {
        return name;
    }

    public boolean worksAt(DayOfWeek day, LocalTime time) {
        return isActive() && schedule().covers(Weekday.of(day), time);
    }

    public boolean worksThroughout(TimeWindow window) {
        return isActive()
                && window.withinOneDay()
                && schedule().embraces(Weekday.of(window.dayOfWeek()), hoursOf(window));
    }

    private static TimeRange hoursOf(TimeWindow window) {
        return new TimeRange(window.startTime(), window.endTime());
    }

    public void describeAs(PractitionerDetails details) {
        this.name = details.name();
        this.licenseNumber = details.license().orElse(null);
    }

    public void deactivate() {
        status = PractitionerStatus.INACTIVE;
    }

    public void follow(WeeklySchedule schedule) {
        availability.clear();
        schedule.periods().forEach(period -> availability.add(new AvailabilitySlot(this, period)));
    }

    public WeeklySchedule schedule() {
        return new WeeklySchedule(availability.stream().map(AvailabilitySlot::period).toList());
    }

    public PractitionerSnapshot snapshot() {
        return new PractitionerSnapshot(id, new PractitionerDetails(name, licenseNumber), status, schedule());
    }
}
