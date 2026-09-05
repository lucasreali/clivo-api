package com.example.clivoapi.core.encounter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.extension.CompletedEncounter;
import com.example.clivoapi.common.extension.EncounterCompletionListener;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

class EncounterCompletionExtensionTest extends EncounterFixture {

    private static final String REFUSAL = "the clinic is closed for the day";

    @TestConfiguration
    static class Bystanders {

        @Bean
        CompletionLog completionLog() {
            return new CompletionLog();
        }

        @Bean
        EncounterCompletionListener completionRecorder(CompletionLog log) {
            return log::add;
        }

        @Bean
        EncounterCompletionListener nightWatch(CompletionLog log) {
            return encounter -> log.refuseWhenClosed();
        }
    }

    static final class CompletionLog {

        private final List<CompletedEncounter> announced = new ArrayList<>();

        private boolean closed;

        void add(CompletedEncounter encounter) {
            announced.add(encounter);
        }

        List<CompletedEncounter> announced() {
            return List.copyOf(announced);
        }

        void close() {
            closed = true;
        }

        void refuseWhenClosed() {
            if (!closed) {
                return;
            }
            throw new BusinessException(REFUSAL);
        }

        void reset() {
            announced.clear();
            closed = false;
        }
    }

    @Autowired
    private CompletionLog log;

    private Long templateId;

    @BeforeEach
    void openTheClinic() {
        log.reset();
        openClinic("TEST-COMPLETION-EXT");
        templateId = publishTemplate("Anamnesis", complaintWith("complaint", "LONG_TEXT"));
    }

    @Test
    void aListenerDeclaredOutsideTheProductionCodeIsCalledJustByExisting() {
        Long id = openWalkIn();

        completeAsPractitioner(id);

        assertThat(log.announced()).singleElement().satisfies(announced -> {
            assertThat(announced.encounterId()).isEqualTo(id);
            assertThat(announced.customerId()).isEqualTo(customerId());
            assertThat(announced.practitionerId()).isEqualTo(practitionerId());
            assertThat(announced.serviceId()).isEqualTo(serviceId());
        });
    }

    @Test
    void anEncounterStillOpenAnnouncesNothing() {
        openWalkIn();

        assertThat(log.announced()).isEmpty();
    }

    @Test
    void aListenerThatRefusesLeavesTheEncounterOpen() {
        Long id = openWalkIn();
        log.close();

        assertThatThrownBy(() -> completeAsPractitioner(id))
                .isInstanceOf(BusinessException.class)
                .hasMessage(REFUSAL);

        assertThat(reopen(id).isCompleted()).isFalse();
    }

    private Long openWalkIn() {
        return encounters
                .open(EncounterOpening.walkIn(customerId(), practitionerId(), serviceId(), templateId))
                .id();
    }
}
