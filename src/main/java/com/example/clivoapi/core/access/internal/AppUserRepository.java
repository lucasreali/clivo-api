package com.example.clivoapi.core.access.internal;

import com.example.clivoapi.core.access.AppUser;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByClinicCodeAndEmail(String clinicCode, String email);

    Optional<AppUser> findByClinicIsNullAndEmail(String email);

    boolean existsByClinicIdAndEmail(Long clinicId, String email);

    List<AppUser> findByClinicIdOrderByNameAsc(Long clinicId);
}
