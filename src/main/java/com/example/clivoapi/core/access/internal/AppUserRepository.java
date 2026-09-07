package com.example.clivoapi.core.access.internal;

import com.example.clivoapi.core.access.AppUser;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {

    Optional<AppUser> findByClinicCodeAndEmail(String clinicCode, String email);

    Optional<AppUser> findByClinicIsNullAndEmail(String email);

    boolean existsByClinicIdAndEmail(UUID clinicId, String email);

    List<AppUser> findByClinicIdOrderByNameAsc(UUID clinicId);
}
