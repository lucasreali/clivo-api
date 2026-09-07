package com.example.clivoapi.patterns.factory.internal;

import com.example.clivoapi.patterns.factory.DeclaredChart;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeclaredChartRepository extends JpaRepository<DeclaredChart, UUID> {

    Optional<DeclaredChart> findByComponentAndVariant(String component, String variant);
}
