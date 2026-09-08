package com.example.clivoapi.common.extension;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Optional;

@Schema(
        description =
                """
                One condition recorded on one region of a special component. Within a single field value, \
                no region and part may be marked twice: `{"region":"26","parts":["oclusal"]}` and a second \
                marking of `26`/`oclusal` are refused together, whatever their marks. Two markings on \
                different parts of the same region are accepted.""")
public record RegionMarking(
        @Schema(description = "Code of the marked region, from ComponentDescriptor.regions[].code.")
                String region,
        @Schema(
                        description =
                                """
                                Parts of the region this condition covers, each from the region's own \
                                `parts`. Empty or absent marks the region as a whole. The vocabulary \
                                entry decides which is allowed: `appliesTo` REGION demands it be empty, \
                                PART demands at least one, ANY accepts both. Several parts in one marking \
                                are one clinical fact sharing one note; "every part" is the explicit full \
                                list, never a wildcard.""")
                List<String> parts,
        @Schema(description = "Code of the condition, from ComponentDescriptor.vocabulary[].code.")
                String mark,
        @Schema(
                        maxLength = NOTE_LIMIT,
                        description = "Free note the practitioner wrote about this marking.")
                String note) {

    public static final int NOTE_LIMIT = 400;

    public RegionMarking {
        parts = List.copyOf(Optional.ofNullable(parts).orElseGet(List::of));
    }

    public boolean coversTheWholeRegion() {
        return parts.isEmpty();
    }

    public List<String> markedParts() {
        return parts;
    }

    public Optional<String> writtenNote() {
        return Optional.ofNullable(note);
    }
}
