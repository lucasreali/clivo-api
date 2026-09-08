package com.example.clivoapi.common.openapi;

import com.example.clivoapi.common.extension.RegionMarking;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import java.util.List;
import java.util.Map;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.stereotype.Component;

@Component
class RecordValueSchema implements OpenApiCustomizer {

    private static final String RECORD_VALUE = "RecordValue";

    private static final String MARKING = "RegionMarking";

    private static final List<String> MARKING_DEMANDS = List.of("region", "mark");

    private static final String SHEET_FIELD = "SheetField";

    private static final String FILLING_REQUEST = "RecordFillingRequest";

    private static final String DESCRIPTION =
            """
            One value of a record field. A plain field holds text or a number; a field that carries a \
            ComponentDescriptor holds the list of markings drawn on its regions. Read `SheetField.fieldType` \
            and the presence of `SheetField.descriptor` to know which shape applies.""";

    private static final String MARKINGS =
            """
            Every marking recorded on this field in this encounter. A region and part may appear at most \
            once across the whole list; a repeat is refused when the draft is saved, not only on \
            completion.""";

    @Override
    public void customise(OpenAPI api) {
        Map<String, Schema> schemas = api.getComponents().getSchemas();
        declareMarking(schemas);
        schemas.put(RECORD_VALUE, recordValue());
        typeFieldValue(schemas.get(SHEET_FIELD));
        typeSubmittedValues(schemas.get(FILLING_REQUEST));
    }

    private void declareMarking(Map<String, Schema> schemas) {
        ModelConverters.getInstance()
                .readAll(RegionMarking.class)
                .forEach((name, schema) -> schemas.putIfAbsent(name, asObject(schema)));
    }

    private Schema<?> asObject(Schema<?> schema) {
        return new ObjectSchema()
                .properties(schema.getProperties())
                .description(schema.getDescription())
                .required(MARKING_DEMANDS);
    }

    private Schema<?> recordValue() {
        return new ComposedSchema()
                .oneOf(List.of(new StringSchema(), new NumberSchema(), markingList()))
                .description(DESCRIPTION);
    }

    private Schema<?> markingList() {
        return new ArraySchema().items(new Schema<>().$ref(reference(MARKING))).description(MARKINGS);
    }

    private void typeFieldValue(Schema<?> sheetField) {
        properties(sheetField).ifPresent(fields -> fields.put("value", reference()));
    }

    private void typeSubmittedValues(Schema<?> request) {
        properties(request)
                .map(fields -> fields.get("values"))
                .ifPresent(values -> values.setAdditionalProperties(reference()));
    }

    private java.util.Optional<Map<String, Schema>> properties(Schema<?> schema) {
        return java.util.Optional.ofNullable(schema).map(Schema::getProperties);
    }

    private Schema<?> reference() {
        return new Schema<>().$ref(reference(RECORD_VALUE));
    }

    private String reference(String name) {
        return "#/components/schemas/%s".formatted(name);
    }
}
