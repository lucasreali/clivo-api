package com.example.clivoapi.core.practitioner.internal;

import com.example.clivoapi.core.practitioner.PractitionerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/practitioners")
@Tag(name = "Practitioners", description = "Who attends, and when they are available")
class PractitionerController {

    private final PractitionerService practitioners;

    PractitionerController(PractitionerService practitioners) {
        this.practitioners = practitioners;
    }

    @Operation(operationId = "registerPractitioner", summary = "Register a practitioner")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    PractitionerView register(@Valid @RequestBody PractitionerRequest request) {
        return PractitionerView.of(practitioners.register(request.toDetails()));
    }

    @Operation(operationId = "listPractitioners", summary = "List the practitioners")
    @GetMapping
    List<PractitionerView> list() {
        return practitioners.findAll().stream().map(PractitionerView::of).toList();
    }

    @Operation(operationId = "getPractitioner", summary = "Read one practitioner with their availability")
    @GetMapping("/{id}")
    PractitionerView findOne(@PathVariable UUID id) {
        return PractitionerView.of(practitioners.findOne(id));
    }

    @Operation(operationId = "describePractitioner", summary = "Redescribe a practitioner")
    @PutMapping("/{id}")
    PractitionerView describe(@PathVariable UUID id, @Valid @RequestBody PractitionerRequest request) {
        return PractitionerView.of(practitioners.describe(id, request.toDetails()));
    }

    @Operation(operationId = "deactivatePractitioner", summary = "Deactivate a practitioner, keeping their history")
    @PostMapping("/{id}/deactivation")
    PractitionerView deactivate(@PathVariable UUID id) {
        return PractitionerView.of(practitioners.deactivate(id));
    }

    @Operation(operationId = "setPractitionerAvailability", summary = "Replace the weekly availability a practitioner follows")
    @PutMapping("/{id}/availability")
    PractitionerView follow(@PathVariable UUID id, @Valid @RequestBody ScheduleRequest request) {
        return PractitionerView.of(practitioners.follow(id, request.toSchedule()));
    }

    @Operation(operationId = "getPractitionerAttendance", summary = "Answer whether a practitioner attends at a given weekday and time")
    @GetMapping("/{id}/attendance")
    AttendanceView worksAt(
            @PathVariable UUID id,
            @RequestParam DayOfWeek weekday,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time) {
        return AttendanceView.of(weekday, time, practitioners.worksAt(id, weekday, time));
    }
}
