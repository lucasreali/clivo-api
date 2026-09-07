package com.example.clivoapi.patterns.factory;

import java.util.Map;
import java.util.Optional;

final class Marking {

    private final Map<?, ?> entries;

    Marking(Map<?, ?> entries) {
        this.entries = entries;
    }

    Optional<String> region() {
        return textOf("region");
    }

    Optional<String> part() {
        return textOf("part");
    }

    Optional<String> mark() {
        return textOf("mark");
    }

    private Optional<String> textOf(String key) {
        return Optional.ofNullable(entries.get(key)).map(Object::toString);
    }
}
