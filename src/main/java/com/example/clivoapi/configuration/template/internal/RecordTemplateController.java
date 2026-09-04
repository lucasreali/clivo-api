package com.example.clivoapi.configuration.template.internal;

import com.example.clivoapi.configuration.template.RecordTemplateService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/record-templates")
class RecordTemplateController {

    private final RecordTemplateService templates;

    RecordTemplateController(RecordTemplateService templates) {
        this.templates = templates;
    }

    @GetMapping
    List<RecordTemplateView> list() {
        return templates.findAll().stream().map(RecordTemplateView::of).toList();
    }

    @GetMapping("/{id}")
    RecordTemplateView findOne(@PathVariable Long id) {
        return RecordTemplateView.of(templates.findOne(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    RecordTemplateView draft(@Valid @RequestBody RecordTemplateRequest request) {
        return RecordTemplateView.of(
                templates.draft(request.name(), request.requiresModule(), request.content()));
    }

    @PutMapping("/{id}")
    RecordTemplateView redefine(@PathVariable Long id, @Valid @RequestBody RecordTemplateRequest request) {
        return RecordTemplateView.of(templates.redefine(id, request.content()));
    }

    @PostMapping("/{id}/publication")
    RecordTemplateView publish(@PathVariable Long id) {
        return RecordTemplateView.of(templates.publish(id));
    }
}
