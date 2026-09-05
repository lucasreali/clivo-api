package com.example.clivoapi.modules.batch.internal;

import com.example.clivoapi.modules.batch.Batch;
import com.example.clivoapi.modules.batch.BatchStatus;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchRepository extends JpaRepository<Batch, Long> {

    List<Batch> findByProductIdOrderByExpiresOnAsc(Long productId);

    List<Batch> findByProductIdAndStatusOrderByExpiresOnAsc(Long productId, BatchStatus status);

    List<Batch> findByStatusAndExpiresOnLessThanEqualOrderByExpiresOnAsc(BatchStatus status, LocalDate limit);
}
