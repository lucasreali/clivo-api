package com.example.clivoapi.configuration.template.internal;

import com.example.clivoapi.configuration.template.RecordTemplate;
import com.example.clivoapi.configuration.template.RecordTemplateStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecordTemplateRepository extends JpaRepository<RecordTemplate, UUID> {

    List<RecordTemplate> findByNameAndStatus(String name, RecordTemplateStatus status);

    List<RecordTemplate> findAllByOrderByNameAscVersionAsc();

    Optional<RecordTemplate> findTopByNameOrderByVersionDesc(String name);
}
