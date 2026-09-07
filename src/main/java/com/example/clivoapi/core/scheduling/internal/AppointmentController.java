package com.example.clivoapi.core.scheduling.internal;

import com.example.clivoapi.core.scheduling.SchedulingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
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
@RequestMapping("/api/appointments")
@Tag(name = "Appointments", description = "The agenda: booking, rescheduling and what happened to each slot")
class AppointmentController {

    private final SchedulingService scheduling;

    AppointmentController(SchedulingService scheduling) {
        this.scheduling = scheduling;
    }

    @Operation(operationId = "scheduleAppointment", summary = "Schedule an appointment, refused on overlap, block or unavailability")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    AppointmentView schedule(@Valid @RequestBody AppointmentRequest request) {
        return AppointmentView.of(scheduling.schedule(request.toBooking()));
    }

    @Operation(operationId = "getDayPanel", summary = "List a day's appointments")
    @GetMapping
    List<AppointmentView> dayPanel(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate day) {
        return scheduling.dayPanel(day).stream().map(AppointmentView::of).toList();
    }

    @Operation(operationId = "getAppointment", summary = "Read one appointment")
    @GetMapping("/{id}")
    AppointmentView findOne(@PathVariable UUID id) {
        return AppointmentView.of(scheduling.findOne(id));
    }

    @Operation(operationId = "rescheduleAppointment", summary = "Move an appointment to another time, within the allowed notice")
    @PutMapping("/{id}/schedule")
    AppointmentView reschedule(@PathVariable UUID id, @Valid @RequestBody RescheduleRequest request) {
        return AppointmentView.of(scheduling.reschedule(id, request.start()));
    }

    @Operation(operationId = "cancelAppointment", summary = "Cancel an appointment, stating the reason")
    @PostMapping("/{id}/cancellation")
    AppointmentView cancel(@PathVariable UUID id, @RequestBody ReasonRequest request) {
        return AppointmentView.of(scheduling.cancel(id, request.toReason()));
    }

    @Operation(operationId = "checkInAppointment", summary = "Register the customer's arrival")
    @PostMapping("/{id}/arrival")
    AppointmentView checkIn(@PathVariable UUID id) {
        return AppointmentView.of(scheduling.checkIn(id));
    }

    @Operation(operationId = "markAppointmentNoShow", summary = "Mark the customer as a no-show, stating the reason")
    @PostMapping("/{id}/absence")
    AppointmentView markNoShow(@PathVariable UUID id, @RequestBody ReasonRequest request) {
        return AppointmentView.of(scheduling.markNoShow(id, request.toReason()));
    }
}
