package br.com.petos.project.service;

import br.com.petos.project.enums.Role;
import br.com.petos.project.support.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Fluxo de vacinação preventiva")
class VaccinationFlowIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("Vacina próxima do vencimento gera alerta preventivo vinculado ao pet")
    void expiringVaccineGeneratesAlert() throws Exception {
        String tutor = registerAndGetToken(Role.TUTOR);
        String clinica = registerAndGetToken(Role.CLINICA);
        Long petId = createPet(tutor, "Thor");

        mockMvc.perform(authenticated(post("/vaccines"), clinica)
                        .content(vaccinePayload(petId, LocalDate.now(), LocalDate.now().plusDays(10))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("EXPIRING_SOON"))
                .andExpect(jsonPath("$.expiringSoon").value(true));

        mockMvc.perform(authenticated(get("/pets/" + petId + "/alerts"), tutor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].type").value("VACCINE_DUE"))
                .andExpect(jsonPath("$[0].petId").value(petId));
    }

    @Test
    @DisplayName("Vacina vencida gera alerta de vacina em atraso")
    void overdueVaccineGeneratesOverdueAlert() throws Exception {
        String tutor = registerAndGetToken(Role.TUTOR);
        String clinica = registerAndGetToken(Role.CLINICA);
        Long petId = createPet(tutor, "Bob");

        mockMvc.perform(authenticated(post("/vaccines"), clinica)
                        .content(vaccinePayload(petId, LocalDate.now().minusMonths(13), LocalDate.now().minusDays(5))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("OVERDUE"));

        mockMvc.perform(authenticated(get("/pets/" + petId + "/alerts"), tutor))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].type").value("VACCINE_OVERDUE"));
    }

    @Test
    @DisplayName("Atualizar a mesma vacina não duplica o alerta")
    void updatingVaccineDoesNotDuplicateAlert() throws Exception {
        String tutor = registerAndGetToken(Role.TUTOR);
        String clinica = registerAndGetToken(Role.CLINICA);
        Long petId = createPet(tutor, "Luna");

        String vaccineId = readField(mockMvc.perform(authenticated(post("/vaccines"), clinica)
                        .content(vaccinePayload(petId, LocalDate.now(), LocalDate.now().plusDays(20))))
                .andExpect(status().isCreated())
                .andReturn(), "id");

        mockMvc.perform(authenticated(put("/vaccines/" + vaccineId), clinica)
                        .content(vaccinePayload(petId, LocalDate.now(), LocalDate.now().plusDays(5))))
                .andExpect(status().isOk());
        mockMvc.perform(authenticated(put("/vaccines/" + vaccineId), clinica)
                        .content(vaccinePayload(petId, LocalDate.now(), LocalDate.now().plusDays(3))))
                .andExpect(status().isOk());

        mockMvc.perform(authenticated(get("/pets/" + petId + "/alerts"), tutor))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("Regularizar a vacina remove o alerta preventivo pendente")
    void regularizingVaccineRemovesAlert() throws Exception {
        String tutor = registerAndGetToken(Role.TUTOR);
        String clinica = registerAndGetToken(Role.CLINICA);
        Long petId = createPet(tutor, "Nina");

        String vaccineId = readField(mockMvc.perform(authenticated(post("/vaccines"), clinica)
                        .content(vaccinePayload(petId, LocalDate.now(), LocalDate.now().plusDays(10))))
                .andReturn(), "id");

        mockMvc.perform(authenticated(put("/vaccines/" + vaccineId), clinica)
                        .content(vaccinePayload(petId, LocalDate.now(), LocalDate.now().plusYears(1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPLIED"));

        mockMvc.perform(authenticated(get("/pets/" + petId + "/alerts"), tutor))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Vacina em dia não gera alerta")
    void vaccineInGoodStandingDoesNotGenerateAlert() throws Exception {
        String tutor = registerAndGetToken(Role.TUTOR);
        String clinica = registerAndGetToken(Role.CLINICA);
        Long petId = createPet(tutor, "Thor");

        mockMvc.perform(authenticated(post("/vaccines"), clinica)
                        .content(vaccinePayload(petId, LocalDate.now(), LocalDate.now().plusYears(1))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("APPLIED"))
                .andExpect(jsonPath("$.expiringSoon").value(false));

        mockMvc.perform(authenticated(get("/pets/" + petId + "/alerts"), tutor))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Vencimento anterior à aplicação é rejeitado com 422")
    void inconsistentDatesAreRejected() throws Exception {
        String tutor = registerAndGetToken(Role.TUTOR);
        String clinica = registerAndGetToken(Role.CLINICA);
        Long petId = createPet(tutor, "Thor");

        mockMvc.perform(authenticated(post("/vaccines"), clinica)
                        .content(vaccinePayload(petId, LocalDate.now().minusDays(1), LocalDate.now().minusDays(10))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("Business Rule Violation"));
    }

    @Test
    @DisplayName("Data de aplicação futura é rejeitada com 400")
    void futureApplicationDateIsRejected() throws Exception {
        String tutor = registerAndGetToken(Role.TUTOR);
        String clinica = registerAndGetToken(Role.CLINICA);
        Long petId = createPet(tutor, "Thor");

        mockMvc.perform(authenticated(post("/vaccines"), clinica)
                        .content(vaccinePayload(petId, LocalDate.now().plusDays(5), LocalDate.now().plusYears(1))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.applicationDate").exists());
    }

    @Test
    @DisplayName("Pets com vacina vencendo listam apenas os pets do próprio tutor")
    void expiringReportIsScopedToOwner() throws Exception {
        String dono = registerAndGetToken(Role.TUTOR);
        String outro = registerAndGetToken(Role.TUTOR);
        String clinica = registerAndGetToken(Role.CLINICA);
        Long petId = createPet(dono, "Thor");

        mockMvc.perform(authenticated(post("/vaccines"), clinica)
                        .content(vaccinePayload(petId, LocalDate.now(), LocalDate.now().plusDays(10))))
                .andExpect(status().isCreated());

        mockMvc.perform(authenticated(get("/pets/vaccines/expiring"), dono))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
        mockMvc.perform(authenticated(get("/pets/vaccines/expiring"), outro))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    private String vaccinePayload(Long petId, LocalDate applicationDate, LocalDate dueDate) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("petId", petId);
        payload.put("name", "Antirrábica");
        payload.put("applicationDate", applicationDate.toString());
        payload.put("dueDate", dueDate.toString());
        return json(payload);
    }
}

