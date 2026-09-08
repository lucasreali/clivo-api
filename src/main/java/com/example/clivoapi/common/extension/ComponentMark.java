package com.example.clivoapi.common.extension;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "One condition a special component accepts, with the colour the legend draws it in.")
public record ComponentMark(
        String code,
        String label,
        @Schema(description = "Colour the legend draws this condition in, as a CSS hex value.") String rendering,
        @Schema(
                        description =
                                """
                                What this condition may be marked on. REGION demands `parts` be empty                                 (a whole tooth is absent, crowned, implanted); PART demands at least one                                 part (a caries sits on a face); ANY accepts either.""")
                MarkTarget appliesTo) {

    public boolean accepts(RegionMarking marking) {
        return appliesTo.accepts(marking);
    }
}
