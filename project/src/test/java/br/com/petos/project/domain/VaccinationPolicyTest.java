package br.com.petos.project.domain;

import br.com.petos.project.enums.AlertType;
import br.com.petos.project.enums.VaccineStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Política de vacinação do PetOS")
class VaccinationPolicyTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 8, 18);

    @Nested
    @DisplayName("Resolução de status")
    class StatusResolution {

        @Test
        @DisplayName("Sem vencimento e já aplicada: APPLIED")
        void appliedWithoutDueDate() {
            assertThat(VaccinationPolicy.resolveStatus(TODAY.minusDays(10), null, TODAY))
                    .isEqualTo(VaccineStatus.APPLIED);
        }

        @Test
        @DisplayName("Sem vencimento e não aplicada: PENDING")
        void pendingWithoutDueDate() {
            assertThat(VaccinationPolicy.resolveStatus(null, null, TODAY))
                    .isEqualTo(VaccineStatus.PENDING);
        }

        @Test
        @DisplayName("Vencimento no passado: OVERDUE")
        void overdue() {
            assertThat(VaccinationPolicy.resolveStatus(TODAY.minusYears(1), TODAY.minusDays(1), TODAY))
                    .isEqualTo(VaccineStatus.OVERDUE);
        }

        @Test
        @DisplayName("Vencimento dentro da janela preventiva: EXPIRING_SOON")
        void expiringSoon() {
            assertThat(VaccinationPolicy.resolveStatus(TODAY.minusMonths(11), TODAY.plusDays(10), TODAY))
                    .isEqualTo(VaccineStatus.EXPIRING_SOON);
        }

        @Test
        @DisplayName("Vencimento exatamente no limite da janela: EXPIRING_SOON")
        void expiringSoonOnWindowBoundary() {
            LocalDate boundary = TODAY.plusDays(VaccinationPolicy.EXPIRY_WARNING_WINDOW_DAYS);
            assertThat(VaccinationPolicy.resolveStatus(TODAY.minusMonths(11), boundary, TODAY))
                    .isEqualTo(VaccineStatus.EXPIRING_SOON);
        }

        @Test
        @DisplayName("Vencimento distante e já aplicada: APPLIED")
        void appliedWithDistantDueDate() {
            assertThat(VaccinationPolicy.resolveStatus(TODAY.minusDays(1), TODAY.plusMonths(11), TODAY))
                    .isEqualTo(VaccineStatus.APPLIED);
        }

        @Test
        @DisplayName("Vencimento distante e não aplicada: PENDING")
        void pendingWithDistantDueDate() {
            assertThat(VaccinationPolicy.resolveStatus(null, TODAY.plusMonths(11), TODAY))
                    .isEqualTo(VaccineStatus.PENDING);
        }

        @Test
        @DisplayName("Aplicação agendada para o futuro não conta como aplicada")
        void futureApplicationIsNotApplied() {
            assertThat(VaccinationPolicy.resolveStatus(TODAY.plusDays(5), TODAY.plusMonths(11), TODAY))
                    .isEqualTo(VaccineStatus.PENDING);
        }
    }

    @Nested
    @DisplayName("Sinalização de vencimento próximo")
    class ExpiringSoonFlag {

        @Test
        @DisplayName("Sem vencimento nunca sinaliza")
        void noDueDate() {
            assertThat(VaccinationPolicy.isExpiringSoon(VaccineStatus.PENDING, null, TODAY)).isFalse();
        }

        @Test
        @DisplayName("Vacina resolvida (APPLIED) não sinaliza")
        void appliedDoesNotFlag() {
            assertThat(VaccinationPolicy.isExpiringSoon(VaccineStatus.APPLIED, TODAY.plusDays(5), TODAY)).isFalse();
        }

        @Test
        @DisplayName("Dentro da janela sinaliza")
        void withinWindowFlags() {
            assertThat(VaccinationPolicy.isExpiringSoon(VaccineStatus.EXPIRING_SOON, TODAY.plusDays(5), TODAY)).isTrue();
        }

        @Test
        @DisplayName("Vencida sinaliza")
        void overdueFlags() {
            assertThat(VaccinationPolicy.isExpiringSoon(VaccineStatus.OVERDUE, TODAY.minusDays(5), TODAY)).isTrue();
        }

        @Test
        @DisplayName("Fora da janela não sinaliza")
        void outsideWindowDoesNotFlag() {
            assertThat(VaccinationPolicy.isExpiringSoon(VaccineStatus.PENDING, TODAY.plusDays(60), TODAY)).isFalse();
        }
    }

    @Nested
    @DisplayName("Alerta preventivo devido")
    class PreventiveAlert {

        @Test
        @DisplayName("Vencida gera alerta de vacina vencida")
        void overdueGeneratesAlert() {
            assertThat(VaccinationPolicy.preventiveAlertType(VaccineStatus.OVERDUE))
                    .contains(AlertType.VACCINE_OVERDUE);
        }

        @Test
        @DisplayName("Próxima do vencimento gera alerta de vencimento")
        void expiringSoonGeneratesAlert() {
            assertThat(VaccinationPolicy.preventiveAlertType(VaccineStatus.EXPIRING_SOON))
                    .contains(AlertType.VACCINE_DUE);
        }

        @Test
        @DisplayName("Aplicada e pendente distante não geram alerta")
        void resolvedDoesNotGenerateAlert() {
            assertThat(VaccinationPolicy.preventiveAlertType(VaccineStatus.APPLIED)).isEmpty();
            assertThat(VaccinationPolicy.preventiveAlertType(VaccineStatus.PENDING)).isEmpty();
        }
    }

    @Test
    @DisplayName("A janela preventiva é única e vale 30 dias")
    void warningThreshold() {
        assertThat(VaccinationPolicy.EXPIRY_WARNING_WINDOW_DAYS).isEqualTo(30);
        assertThat(VaccinationPolicy.warningThreshold(TODAY)).isEqualTo(TODAY.plusDays(30));
    }
}

