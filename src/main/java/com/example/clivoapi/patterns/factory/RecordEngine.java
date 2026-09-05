package com.example.clivoapi.patterns.factory;

import com.example.clivoapi.common.extension.RecordAssembly;
import com.example.clivoapi.common.extension.RecordFilling;
import com.example.clivoapi.common.extension.RecordSheet;
import com.example.clivoapi.common.extension.RecordValues;
import com.example.clivoapi.common.extension.SheetField;
import com.example.clivoapi.common.extension.SheetSection;
import com.example.clivoapi.configuration.template.RecordTemplateService;
import com.example.clivoapi.configuration.template.SectionContent;
import com.example.clivoapi.configuration.template.TemplateContent;
import com.example.clivoapi.configuration.template.TemplateSnapshot;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RecordEngine implements RecordAssembly {

    private final RecordTemplateService templates;
    private final FieldCatalog catalogue;

    RecordEngine(RecordTemplateService templates, FieldCatalog catalogue) {
        this.templates = templates;
        this.catalogue = catalogue;
    }

    @Override
    public RecordSheet assemble(RecordFilling filling) {
        TemplateSnapshot template = templates.findOne(filling.templateId());
        return new RecordSheet(
                template.id(), template.name(), template.version(), sheetSections(template, filling.values()));
    }

    @Override
    public void validate(RecordFilling filling) {
        TemplateSnapshot template = templates.findOne(filling.templateId());
        RecordValues values = filling.values();
        fieldsOf(template).forEach(field -> field.check(values));
    }

    private List<SheetSection> sheetSections(TemplateSnapshot template, RecordValues values) {
        return sectionsOf(template).stream()
                .map(section -> sheetSection(section, values))
                .toList();
    }

    private SheetSection sheetSection(SectionContent section, RecordValues values) {
        return new SheetSection(section.name(), filledFields(section, values));
    }

    private List<SheetField> filledFields(SectionContent section, RecordValues values) {
        return catalogue.fieldsOf(section).stream()
                .map(field -> field.fill(values))
                .toList();
    }

    private List<Field> fieldsOf(TemplateSnapshot template) {
        return sectionsOf(template).stream()
                .flatMap(section -> catalogue.fieldsOf(section).stream())
                .toList();
    }

    private List<SectionContent> sectionsOf(TemplateSnapshot template) {
        TemplateContent content = template.content();
        return content.sections();
    }
}
