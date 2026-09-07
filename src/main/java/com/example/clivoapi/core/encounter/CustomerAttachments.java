package com.example.clivoapi.core.encounter;

import static java.util.stream.Collectors.groupingBy;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record CustomerAttachments(Map<UUID, List<AttachmentSnapshot>> byEncounter) {

    public CustomerAttachments {
        byEncounter = Map.copyOf(byEncounter);
    }

    public static CustomerAttachments of(List<AttachmentSnapshot> attachments) {
        return new CustomerAttachments(attachments.stream().collect(groupingBy(AttachmentSnapshot::encounterId)));
    }

    public List<AttachmentSnapshot> of(UUID encounterId) {
        return byEncounter.getOrDefault(encounterId, List.of());
    }

    public List<AttachmentSnapshot> all() {
        return byEncounter.values().stream().flatMap(List::stream).toList();
    }
}
