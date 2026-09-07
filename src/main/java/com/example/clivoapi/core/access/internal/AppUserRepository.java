package com.example.clivoapi.core.access.internal;

import com.example.clivoapi.core.access.AppUser;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {

    Optional<AppUser> findByEmail(String email);

    boolean existsByEmail(String email);

    List<AppUser> findByClinicIdOrderByNameAsc(UUID clinicId);
}
