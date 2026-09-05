package com.example.clivoapi.modules.inventory.internal;

import com.example.clivoapi.modules.inventory.StockMovement;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    List<StockMovement> findByProductIdOrderByRecordedAtDesc(Long productId);
}
