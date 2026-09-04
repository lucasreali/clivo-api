package com.example.clivoapi.configuration.template;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.extension.ModuleCode;
import com.example.clivoapi.common.tenant.TenantScopedEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Entity
@Table(name = "record_template")
public class RecordTemplate extends TenantScopedEntity {

    private static final short FIRST_VERSION = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private short version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecordTemplateStatus status;

    @Column(name = "cloned_from")
    private Long clonedFrom;

    @Column(name = "requires_module")
    private String requiresModule;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder")
    private List<TemplateSection> sections = new ArrayList<>();

    protected RecordTemplate() {
    }

    public RecordTemplate(String name, String requiresModule) {
        this.name = name;
        this.requiresModule = requiresModule;
        this.version = FIRST_VERSION;
        this.status = RecordTemplateStatus.DRAFT;
        this.createdAt = Instant.now();
    }

    public Long id() {
        return id;
    }

    public String name() {
        return name;
    }

    public short version() {
        return version;
    }

    public TemplateSnapshot snapshot() {
        return new TemplateSnapshot(id, name, version, status, requiredModule().orElse(null), content());
    }

    public TemplateContent content() {
        return new TemplateContent(sections.stream().map(TemplateSection::content).toList());
    }

    public boolean isPublished() {
        return status == RecordTemplateStatus.PUBLISHED;
    }

    public Optional<ModuleCode> requiredModule() {
        return Optional.ofNullable(requiresModule).map(ModuleCode::new);
    }

    public Stream<ModuleCode> requiredModules() {
        return Stream.concat(requiredModule().stream(), fieldModules());
    }

    public void replaceContent(TemplateContent content) {
        requireEditable();
        sections.clear();
        content.sections().forEach(this::addSection);
    }

    public void publish() {
        requireEditable();
        status = RecordTemplateStatus.PUBLISHED;
    }

    public void retire() {
        status = RecordTemplateStatus.RETIRED;
    }

    public RecordTemplate nextVersionAfter(short latestVersion) {
        RecordTemplate copy = new RecordTemplate(name, requiresModule);
        copy.version = (short) (latestVersion + 1);
        copy.clonedFrom = id;
        sections.forEach(section -> copy.sections.add(section.copyInto(copy)));
        return copy;
    }

    private Stream<ModuleCode> fieldModules() {
        return sections.stream()
                .flatMap(TemplateSection::fields)
                .flatMap(field -> field.requiredModule().stream());
    }

    private void addSection(SectionContent content) {
        sections.add(new TemplateSection(this, content, (short) sections.size()));
    }

    private void requireEditable() {
        if (status == RecordTemplateStatus.DRAFT) {
            return;
        }
        throw new BusinessException(
                "record template %s version %d is %s and cannot be changed".formatted(name, version, status));
    }
}
