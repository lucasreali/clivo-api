package com.example.clivoapi.modules.batch.internal;

import com.example.clivoapi.modules.batch.Batch;
import com.example.clivoapi.modules.batch.BatchStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchRepository extends JpaRepository<Batch, UUID> {

    List<Batch> findByProductIdOrderByExpiresOnAsc(UUID productId);

    List<Batch> findByProductIdAndStatusOrderByExpiresOnAsc(UUID productId, BatchStatus status);

    List<Batch> findByStatusAndExpiresOnLessThanEqualOrderByExpiresOnAsc(BatchStatus status, LocalDate limit);
}
