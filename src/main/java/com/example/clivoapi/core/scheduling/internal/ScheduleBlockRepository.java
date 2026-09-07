package com.example.clivoapi.core.scheduling.internal;

import com.example.clivoapi.core.scheduling.ScheduleBlock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleBlockRepository extends JpaRepository<ScheduleBlock, UUID> {

    List<ScheduleBlock> findByPeriodStartsAtLessThanAndPeriodEndsAtGreaterThanOrderByPeriodStartsAtAsc(
            Instant end, Instant start);
}
