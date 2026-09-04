package com.example.clivoapi.core.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.example.clivoapi.common.DatabaseTest;
import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.money.Money;
import com.example.clivoapi.common.tenant.Tenant;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CatalogServiceTest extends DatabaseTest {

    @Autowired
    private CatalogService catalogue;

    @Test
    void theDurationOfTheServiceGivesTheEndOfTheSlot() {
        bindTenant(createTenant("TEST-CATALOG"));
        Long id = catalogue.register(detailsOf("Limpeza", 45, "180.00")).id();

        ServiceSnapshot service = catalogue.findOne(id);

        assertThat(service.details().duration().endFrom(LocalDateTime.of(2026, 9, 4, 14, 30)))
                .isEqualTo(LocalDateTime.of(2026, 9, 4, 15, 15));
        assertThat(service.details().price()).isEqualTo(Money.of("180.00"));
    }

    @Test
    void aNameIsUniqueWithinAClinicAndFreeAcrossClinics() {
        Tenant north = createTenant("TEST-NORTH");
        Tenant south = createTenant("TEST-SOUTH");
        inTenant(north, () -> catalogue.register(detailsOf("Limpeza", 45, "180.00")));

        inTenant(south, () -> catalogue.register(detailsOf("Limpeza", 30, "150.00")));

        bindTenant(north);
        assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> catalogue.register(detailsOf("limpeza", 60, "200.00")))
                .withMessageContaining("already in the catalogue");
    }

    @Test
    void aDurationOutsideTheAllowedRangeIsRefused() {
        assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> ServiceDuration.ofMinutes(600))
                .withMessageContaining("between 5 and 480 minutes");
    }

    @Test
    void aNegativePriceIsRefused() {
        assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> Money.of("-1.00"))
                .withMessageContaining("cannot be negative");
    }

    @Test
    void aDeactivatedServiceStaysInTheCatalogue() {
        bindTenant(createTenant("TEST-CATALOG"));
        Long id = catalogue.register(detailsOf("Limpeza", 45, "180.00")).id();

        assertThat(catalogue.deactivate(id).status()).isEqualTo(ServiceStatus.INACTIVE);
        assertThat(catalogue.findAll()).hasSize(1);
    }

    private ServiceDetails detailsOf(String name, int minutes, String price) {
        return new ServiceDetails(name, ServiceDuration.ofMinutes(minutes), Money.of(price));
    }
}
