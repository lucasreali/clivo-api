package com.example.clivoapi.core.scheduling;

import com.example.clivoapi.common.time.TimeWindow;
import com.example.clivoapi.core.catalog.CatalogService;
import com.example.clivoapi.core.catalog.Service;
import com.example.clivoapi.core.customer.CustomerService;
import com.example.clivoapi.core.practitioner.PractitionerService;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class AppointmentAssembler {

    private final CustomerService customers;
    private final PractitionerService practitioners;
    private final CatalogService catalogue;

    AppointmentAssembler(CustomerService customers, PractitionerService practitioners, CatalogService catalogue) {
        this.customers = customers;
        this.practitioners = practitioners;
        this.catalogue = catalogue;
    }

    public Appointment assemble(AppointmentBooking booking) {
        Service service = catalogue.reference(booking.serviceId());
        return new Appointment(
                customers.reference(booking.customerId()),
                practitioners.reference(booking.practitionerId()),
                service,
                windowOf(service, booking.start()));
    }

    private TimeWindow windowOf(Service service, LocalDateTime start) {
        return TimeWindow.of(start, service.endTimeFrom(start));
    }
}
