package com.example.clivoapi.modules.inventory.internal;

import com.example.clivoapi.modules.inventory.StockMovement;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMovementRepository extends JpaRepository<StockMovement, UUID> {

    List<StockMovement> findByProductIdOrderByRecordedAtDesc(UUID productId);
}
