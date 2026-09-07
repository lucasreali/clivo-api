package com.example.clivoapi.configuration.template;

import static org.hibernate.annotations.UuidGenerator.Style.VERSION_7;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "template_section")
public class TemplateSection {

    @Id
    @UuidGenerator(style = VERSION_7)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "record_template_id", nullable = false)
    private RecordTemplate template;

    @Column(nullable = false)
    private String name;

    @Column(name = "sort_order", nullable = false)
    private short sortOrder;

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder")
    private List<TemplateField> fields = new ArrayList<>();

    protected TemplateSection() {
    }

    TemplateSection(RecordTemplate template, SectionContent content, short sortOrder) {
        this.template = template;
        this.name = content.name();
        this.sortOrder = sortOrder;
        content.fields().forEach(this::addField);
    }

    public SectionContent content() {
        return new SectionContent(name, fields.stream().map(TemplateField::content).toList());
    }

    Stream<TemplateField> fields() {
        return fields.stream();
    }

    TemplateSection copyInto(RecordTemplate target) {
        TemplateSection copy = new TemplateSection(target, new SectionContent(name, List.of()), sortOrder);
        fields.forEach(field -> copy.fields.add(field.copyInto(copy)));
        return copy;
    }

    private void addField(FieldContent content) {
        fields.add(new TemplateField(this, content, (short) fields.size()));
    }
}
