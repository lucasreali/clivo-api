package com.example.clivoapi.core.scheduling.internal;

import com.example.clivoapi.core.scheduling.Appointment;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    List<Appointment> findByPractitionerIdAndPeriodStartsAtLessThanAndPeriodEndsAtGreaterThan(
            UUID practitionerId, Instant end, Instant start);

    List<Appointment> findByPeriodStartsAtGreaterThanEqualAndPeriodStartsAtLessThanOrderByPeriodStartsAtAsc(
            Instant start, Instant end);
}
