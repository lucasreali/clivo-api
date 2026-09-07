package com.example.clivoapi.core.scheduling;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc(addFilters = false)
class DayPanelApiTest extends SchedulingFixture {

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void openTheClinic() {
        openClinic("TEST-PANEL");
    }

    @Test
    void thePanelListsTheDayInTimeOrder() throws Exception {
        LocalDateTime afternoon = nextWeekAt(DayOfWeek.MONDAY, "14:00");
        UUID later = bookAt(afternoon).id();
        LocalDateTime morning = nextWeekAt(DayOfWeek.MONDAY, "09:00");
        UUID earlier = bookAt(morning).id();

        mockMvc.perform(get("/api/appointments").param("day", morning.toLocalDate().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(earlier.toString()))
                .andExpect(jsonPath("$[0].status").value("SCHEDULED"))
                .andExpect(jsonPath("$[0].practitionerName").value("Dr. Marina"))
                .andExpect(jsonPath("$[1].id").value(later.toString()));
    }

    @Test
    void thePanelIgnoresTheAppointmentsOfAnotherDay() throws Exception {
        LocalDateTime monday = nextWeekAt(DayOfWeek.MONDAY, "09:00");
        bookAt(monday);

        mockMvc.perform(get("/api/appointments").param("day", monday.plusDays(1).toLocalDate().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void theArrivalPutsTheCustomerInTheWaitingLine() throws Exception {
        UUID id = bookAt(nextWeekAt(DayOfWeek.MONDAY, "09:00")).id();

        mockMvc.perform(post("/api/appointments/{id}/arrival", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ARRIVED"))
                .andExpect(jsonPath("$.waiting").value(true));
    }

    @Test
    void theAbsenceIsRegisteredWithItsReason() throws Exception {
        UUID id = bookAt(nextWeekAt(DayOfWeek.MONDAY, "09:00")).id();

        mockMvc.perform(post("/api/appointments/{id}/absence", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Cliente nao compareceu\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NO_SHOW"))
                .andExpect(jsonPath("$.reason").value("Cliente nao compareceu"));
    }

    @Test
    void anAppointmentThatAlreadyArrivedIsNotMarkedAbsent() throws Exception {
        UUID id = bookAt(nextWeekAt(DayOfWeek.MONDAY, "09:00")).id();
        scheduling.checkIn(id);

        mockMvc.perform(post("/api/appointments/{id}/absence", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Cliente nao compareceu\"}"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message")
                        .value("an appointment in status ARRIVED cannot be marked as a no-show"));
    }
}
