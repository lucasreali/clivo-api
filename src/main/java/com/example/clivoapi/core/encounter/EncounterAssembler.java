package com.example.clivoapi.core.encounter;

import com.example.clivoapi.core.catalog.CatalogService;
import com.example.clivoapi.core.customer.CustomerService;
import com.example.clivoapi.core.practitioner.PractitionerService;
import com.example.clivoapi.core.scheduling.Appointment;
import com.example.clivoapi.core.scheduling.AppointmentBook;
import com.example.clivoapi.core.scheduling.AppointmentParticipants;
import org.springframework.stereotype.Component;

@Component
public class EncounterAssembler {

    private final AppointmentBook book;
    private final EncounterParties parties;

    EncounterAssembler(
            AppointmentBook book,
            CustomerService customers,
            PractitionerService practitioners,
            CatalogService catalogue) {
        this.book = book;
        this.parties = new EncounterParties(customers, practitioners, catalogue);
    }

    public Encounter assemble(EncounterOpening opening) {
        return opening.appointment()
                .map(id -> fromAppointment(book.reference(id), opening))
                .orElseGet(() -> fromParties(opening));
    }

    private Encounter fromAppointment(Appointment appointment, EncounterOpening opening) {
        AppointmentParticipants booking = appointment.snapshot().participants();
        return new Encounter(
                appointment,
                parties.customer(booking.customerId()),
                parties.practitioner(booking.practitionerId()),
                parties.service(booking.serviceId()),
                opening.recordTemplateId());
    }

    private Encounter fromParties(EncounterOpening opening) {
        return new Encounter(
                parties.customer(opening.customerId()),
                parties.practitioner(opening.practitionerId()),
                parties.service(opening.serviceId()),
                opening.recordTemplateId());
    }
}
