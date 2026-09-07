package com.example.clivoapi.configuration.template;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.exception.ResourceNotFoundException;
import com.example.clivoapi.common.extension.ModuleActivationState;
import com.example.clivoapi.common.extension.ModuleCode;
import com.example.clivoapi.configuration.template.internal.RecordTemplateRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RecordTemplateService {

    private final RecordTemplateRepository templates;
    private final ModuleActivationState activationState;

    RecordTemplateService(RecordTemplateRepository templates, ModuleActivationState activationState) {
        this.templates = templates;
        this.activationState = activationState;
    }

    public TemplateSnapshot draft(String name, String requiresModule, TemplateContent content) {
        RecordTemplate template = new RecordTemplate(name, requiresModule);
        template.replaceContent(content);
        return templates.save(template).snapshot();
    }

    public TemplateSnapshot redefine(UUID id, TemplateContent content) {
        RecordTemplate template = editableVersionOf(templateOf(id));
        template.replaceContent(content);
        return templates.save(template).snapshot();
    }

    public TemplateSnapshot publish(UUID id) {
        RecordTemplate template = templateOf(id);
        requireModulesActive(template);
        templates.findByNameAndStatus(template.name(), RecordTemplateStatus.PUBLISHED).forEach(RecordTemplate::retire);
        template.publish();
        return templates.save(template).snapshot();
    }

    @Transactional(readOnly = true)
    public List<TemplateSnapshot> findAll() {
        return templates.findAllByOrderByNameAscVersionAsc().stream().map(RecordTemplate::snapshot).toList();
    }

    @Transactional(readOnly = true)
    public TemplateSnapshot findOne(UUID id) {
        return templateOf(id).snapshot();
    }

    private RecordTemplate editableVersionOf(RecordTemplate template) {
        if (template.isPublished()) {
            return template.nextVersionAfter(latestVersionOf(template.name()));
        }
        return template;
    }

    private short latestVersionOf(String name) {
        return templates.findTopByNameOrderByVersionDesc(name).map(RecordTemplate::version).orElse((short) 0);
    }

    private void requireModulesActive(RecordTemplate template) {
        template.requiredModules()
                .filter(this::isInactive)
                .findFirst()
                .ifPresent(this::refuseInactiveModule);
    }

    private boolean isInactive(ModuleCode module) {
        return !activationState.isActive(module);
    }

    private void refuseInactiveModule(ModuleCode module) {
        throw new BusinessException("module %s is not active in this clinic".formatted(module));
    }

    private RecordTemplate templateOf(UUID id) {
        return templates.findById(id).orElseThrow(() -> new ResourceNotFoundException("Record template", id));
    }
}
