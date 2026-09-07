package com.example.clivoapi.core.practitioner;

import com.example.clivoapi.common.exception.ResourceNotFoundException;
import com.example.clivoapi.core.practitioner.internal.PractitionerRepository;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PractitionerService {

    private final PractitionerRepository practitioners;

    PractitionerService(PractitionerRepository practitioners) {
        this.practitioners = practitioners;
    }

    public PractitionerSnapshot register(PractitionerDetails details) {
        return practitioners.save(new Practitioner(details)).snapshot();
    }

    public PractitionerSnapshot describe(UUID id, PractitionerDetails details) {
        Practitioner practitioner = practitionerOf(id);
        practitioner.describeAs(details);
        return practitioners.save(practitioner).snapshot();
    }

    public PractitionerSnapshot deactivate(UUID id) {
        Practitioner practitioner = practitionerOf(id);
        practitioner.deactivate();
        return practitioners.save(practitioner).snapshot();
    }

    public PractitionerSnapshot follow(UUID id, WeeklySchedule schedule) {
        Practitioner practitioner = practitionerOf(id);
        practitioner.follow(schedule);
        return practitioners.save(practitioner).snapshot();
    }

    @Transactional(readOnly = true)
    public boolean worksAt(UUID id, DayOfWeek day, LocalTime time) {
        return practitionerOf(id).worksAt(day, time);
    }

    @Transactional(readOnly = true)
    public List<PractitionerSnapshot> findAll() {
        return practitioners.findAllByOrderByNameAsc().stream().map(Practitioner::snapshot).toList();
    }

    @Transactional(readOnly = true)
    public PractitionerSnapshot findOne(UUID id) {
        return practitionerOf(id).snapshot();
    }

    @Transactional(readOnly = true)
    public Practitioner reference(UUID id) {
        return practitionerOf(id);
    }

    private Practitioner practitionerOf(UUID id) {
        return practitioners.findById(id).orElseThrow(() -> new ResourceNotFoundException("Practitioner", id));
    }
}
