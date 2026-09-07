package com.example.clivoapi.core.access.internal;

import com.example.clivoapi.core.access.ModuleGrant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModuleGrantRepository extends JpaRepository<ModuleGrant, UUID> {

    Optional<ModuleGrant> findByUserIdAndModuleCode(UUID userId, String moduleCode);

    boolean existsByUserIdAndModuleCode(UUID userId, String moduleCode);

    List<ModuleGrant> findByUserIdOrderByModuleCodeAsc(UUID userId);
}
