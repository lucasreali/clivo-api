package com.example.clivoapi.core.encounter;

import com.example.clivoapi.common.extension.CoverageDirectory;
import com.example.clivoapi.common.extension.CoverageNote;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

class CoverageDirectories {

    private final List<CoverageDirectory> directories;

    CoverageDirectories(List<CoverageDirectory> directories) {
        this.directories = List.copyOf(directories);
    }

    Optional<CoverageNote> coverageOf(UUID customerId) {
        return directories.stream()
                .map(directory -> directory.coverageOf(customerId))
                .flatMap(Optional::stream)
                .findFirst();
    }
}
