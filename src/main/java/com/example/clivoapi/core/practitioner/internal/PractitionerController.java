package com.example.clivoapi.core.practitioner.internal;

import com.example.clivoapi.core.practitioner.PractitionerService;
import jakarta.validation.Valid;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
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
class PractitionerController {

    private final PractitionerService practitioners;

    PractitionerController(PractitionerService practitioners) {
        this.practitioners = practitioners;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    PractitionerView register(@Valid @RequestBody PractitionerRequest request) {
        return PractitionerView.of(practitioners.register(request.toDetails()));
    }

    @GetMapping
    List<PractitionerView> list() {
        return practitioners.findAll().stream().map(PractitionerView::of).toList();
    }

    @GetMapping("/{id}")
    PractitionerView findOne(@PathVariable Long id) {
        return PractitionerView.of(practitioners.findOne(id));
    }

    @PutMapping("/{id}")
    PractitionerView describe(@PathVariable Long id, @Valid @RequestBody PractitionerRequest request) {
        return PractitionerView.of(practitioners.describe(id, request.toDetails()));
    }

    @PostMapping("/{id}/deactivation")
    PractitionerView deactivate(@PathVariable Long id) {
        return PractitionerView.of(practitioners.deactivate(id));
    }

    @PutMapping("/{id}/availability")
    PractitionerView follow(@PathVariable Long id, @Valid @RequestBody ScheduleRequest request) {
        return PractitionerView.of(practitioners.follow(id, request.toSchedule()));
    }

    @GetMapping("/{id}/attendance")
    AttendanceView worksAt(
            @PathVariable Long id,
            @RequestParam DayOfWeek weekday,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time) {
        return AttendanceView.of(weekday, time, practitioners.worksAt(id, weekday, time));
    }
}
