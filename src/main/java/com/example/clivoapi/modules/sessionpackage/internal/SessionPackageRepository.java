package com.example.clivoapi.modules.sessionpackage.internal;

import com.example.clivoapi.modules.sessionpackage.SessionPackage;
import com.example.clivoapi.modules.sessionpackage.SessionPackageStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionPackageRepository extends JpaRepository<SessionPackage, UUID> {

    List<SessionPackage> findByCustomerIdOrderByExpiresOnAsc(UUID customerId);

    List<SessionPackage> findByCustomerIdAndServiceIdAndStatusOrderByExpiresOnAsc(
            UUID customerId, UUID serviceId, SessionPackageStatus status);
}
