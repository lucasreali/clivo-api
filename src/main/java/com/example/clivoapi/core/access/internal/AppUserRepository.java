package com.example.clivoapi.core.access.internal;

import com.example.clivoapi.core.access.AppUser;
import com.example.clivoapi.core.access.AppUserStatus;
import com.example.clivoapi.core.access.EmailAddress;
import com.example.clivoapi.core.access.Role;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {

    Optional<AppUser> findByEmail(EmailAddress email);

    boolean existsByEmail(EmailAddress email);

    List<AppUser> findByClinicIdOrderByNameAsc(UUID clinicId);

    Optional<AppUser> findByIdAndClinicId(UUID id, UUID clinicId);

    long countByClinicIdAndRoleAndStatus(UUID clinicId, Role role, AppUserStatus status);
}
