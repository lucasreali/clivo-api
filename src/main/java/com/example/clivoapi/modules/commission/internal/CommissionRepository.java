package com.example.clivoapi.modules.commission.internal;

import com.example.clivoapi.modules.commission.Commission;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommissionRepository extends JpaRepository<Commission, UUID> {

    List<Commission> findByPeriodOrderByIdAsc(LocalDate period);
}
