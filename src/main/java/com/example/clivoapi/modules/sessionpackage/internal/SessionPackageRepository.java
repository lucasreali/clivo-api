package com.example.clivoapi.modules.sessionpackage.internal;

import com.example.clivoapi.modules.sessionpackage.SessionPackage;
import com.example.clivoapi.modules.sessionpackage.SessionPackageStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionPackageRepository extends JpaRepository<SessionPackage, Long> {

    List<SessionPackage> findByCustomerIdOrderByExpiresOnAsc(Long customerId);

    List<SessionPackage> findByCustomerIdAndServiceIdAndStatusOrderByExpiresOnAsc(
            Long customerId, Long serviceId, SessionPackageStatus status);
}
