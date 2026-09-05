package com.example.clivoapi.core.billing;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.clivoapi.common.extension.ParameterCode;
import com.example.clivoapi.common.extension.ParameterValue;
import com.example.clivoapi.configuration.parameter.ClinicParameterService;
import com.example.clivoapi.core.access.Role;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@AutoConfigureMockMvc(addFilters = false)
class FinancialReportApiTest extends BillingFixture {

    private static final ParameterCode ROLE_MODEL = new ParameterCode("role_model");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClinicParameterService parameters;

    @Test
    void receptionIsRefusedTheFinancialReportInASegregatedClinic() throws Exception {
        openClinic("TEST-REPORT-SEG");
        completeAnEncounter();
        parameters.change(ROLE_MODEL, ParameterValue.of("SEGREGATED"));
        signInAs(Role.RECEPTION, "recepcao@clivo.test");

        mockMvc.perform(reportOfThisMonth())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("the financial report is restricted to management in this clinic"));
    }

    @Test
    void theSameReceptionRoleReadsTheReportInASingleRoleClinic() throws Exception {
        openClinic("TEST-REPORT-SINGLE");
        completeAnEncounter();
        parameters.change(ROLE_MODEL, ParameterValue.of("SINGLE"));
        signInAs(Role.RECEPTION, "recepcao@clivo.test");

        mockMvc.perform(reportOfThisMonth())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.invoices").value(1))
                .andExpect(jsonPath("$.gross").value(180.00))
                .andExpect(jsonPath("$.outstanding").value(180.00));
    }

    @Test
    void managementReadsTheReportEvenInASegregatedClinic() throws Exception {
        openClinic("TEST-REPORT-MGMT");
        completeAnEncounter();
        parameters.change(ROLE_MODEL, ParameterValue.of("SEGREGATED"));

        mockMvc.perform(reportOfThisMonth()).andExpect(status().isOk());
    }

    private MockHttpServletRequestBuilder reportOfThisMonth() {
        ReportPeriod period = ReportPeriod.ofMonth(LocalDate.now());
        return get("/api/invoices/report").param("from", period.from().toString()).param("to", period.to().toString());
    }
}
