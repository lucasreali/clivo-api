package com.example.clivoapi.core.scheduling.internal;

import com.example.clivoapi.core.scheduling.ScheduleBlock;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleBlockRepository extends JpaRepository<ScheduleBlock, Long> {

    List<ScheduleBlock> findByPeriodStartsAtLessThanAndPeriodEndsAtGreaterThanOrderByPeriodStartsAtAsc(
            Instant end, Instant start);
}
