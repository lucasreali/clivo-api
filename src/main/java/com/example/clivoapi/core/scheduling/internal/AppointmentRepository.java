package com.example.clivoapi.core.scheduling.internal;

import com.example.clivoapi.core.scheduling.Appointment;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPractitionerIdAndPeriodStartsAtLessThanAndPeriodEndsAtGreaterThan(
            Long practitionerId, Instant end, Instant start);

    List<Appointment> findByPeriodStartsAtGreaterThanEqualAndPeriodStartsAtLessThanOrderByPeriodStartsAtAsc(
            Instant start, Instant end);
}
