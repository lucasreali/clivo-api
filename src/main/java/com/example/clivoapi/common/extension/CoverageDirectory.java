package com.example.clivoapi.common.extension;

import java.util.Optional;
import java.util.UUID;

public interface CoverageDirectory {

    Optional<CoverageNote> coverageOf(UUID customerId);
}
