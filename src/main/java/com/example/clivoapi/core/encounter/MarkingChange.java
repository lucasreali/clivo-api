package com.example.clivoapi.core.encounter;

public record MarkingChange(String region, String part, ChangeKind change, MarkState from, MarkState to) {
}
