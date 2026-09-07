package com.example.clivoapi.core.scheduling;

import com.example.clivoapi.common.exception.ResourceNotFoundException;
import com.example.clivoapi.common.time.TimeWindow;
import com.example.clivoapi.core.practitioner.Practitioner;
import com.example.clivoapi.core.practitioner.PractitionerService;
import com.example.clivoapi.core.scheduling.internal.ScheduleBlockRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ScheduleBlockService {

    private final ScheduleBlockRepository blocks;
    private final PractitionerService practitioners;

    ScheduleBlockService(ScheduleBlockRepository blocks, PractitionerService practitioners) {
        this.blocks = blocks;
        this.practitioners = practitioners;
    }

    public ScheduleBlockSnapshot register(ScheduleBlockDetails details) {
        ScheduleBlock block = new ScheduleBlock(blockedPractitioner(details), details.period(), details.reason());
        return blocks.save(block).snapshot();
    }

    public void release(UUID id) {
        blocks.delete(blockOf(id));
    }

    @Transactional(readOnly = true)
    public List<ScheduleBlockSnapshot> findWithin(TimeWindow period) {
        return within(period).stream().map(ScheduleBlock::snapshot).toList();
    }

    @Transactional(readOnly = true)
    public Optional<ScheduleBlockSnapshot> firstBlocking(UUID practitionerId, TimeWindow period) {
        Practitioner practitioner = practitioners.reference(practitionerId);
        return within(period).stream()
                .filter(block -> block.appliesTo(practitioner))
                .findFirst()
                .map(ScheduleBlock::snapshot);
    }

    private List<ScheduleBlock> within(TimeWindow period) {
        return blocks.findByPeriodStartsAtLessThanAndPeriodEndsAtGreaterThanOrderByPeriodStartsAtAsc(
                period.endsAt(), period.startsAt());
    }

    private Practitioner blockedPractitioner(ScheduleBlockDetails details) {
        return details.practitioner().map(practitioners::reference).orElse(null);
    }

    private ScheduleBlock blockOf(UUID id) {
        return blocks.findById(id).orElseThrow(() -> new ResourceNotFoundException("ScheduleBlock", id));
    }
}
