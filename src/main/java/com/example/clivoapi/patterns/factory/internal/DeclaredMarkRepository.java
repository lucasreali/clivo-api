package com.example.clivoapi.patterns.factory.internal;

import com.example.clivoapi.patterns.factory.DeclaredMark;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeclaredMarkRepository extends JpaRepository<DeclaredMark, UUID> {

    List<DeclaredMark> findByComponentOrderBySortOrder(String component);
}
