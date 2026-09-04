package com.example.clivoapi.core.practitioner.internal;

import com.example.clivoapi.core.practitioner.Practitioner;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PractitionerRepository extends JpaRepository<Practitioner, Long> {

    List<Practitioner> findAllByOrderByNameAsc();
}
