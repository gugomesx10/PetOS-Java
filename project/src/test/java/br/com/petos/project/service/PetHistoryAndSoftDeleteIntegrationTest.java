package br.com.petos.project.service;

import br.com.petos.project.enums.Role;
import br.com.petos.project.support.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Histórico consolidado e soft delete do pet")
class PetHistoryAndSoftDeleteIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("Histórico reúne vacinas, rotinas e alertas do pet")
    void historyConsolidatesEverything() throws Exception {
        String tutor = registerAndGetToken(Role.TUTOR);
        String clinica = registerAndGetToken(Role.CLINICA);
        Long petId = createPet(tutor, "Thor");

        mockMvc.perform(authenticated(post("/vaccines"), clinica)
                        .content(json(Map.of(
                                "petId", petId,
                                "name", "Antirrábica",
                                "applicationDate", LocalDate.now().toString(),
                                "dueDate", LocalDate.now().plusDays(10).toString()))))
                .andExpect(status().isCreated());

        mockMvc.perform(authenticated(post("/routines"), tutor)
                        .content(json(Map.of(
                                "petId", petId,
                                "type", "WALK",
                                "description", "Caminhada",
                                "recordDate", LocalDate.now().toString()))))
                .andExpect(status().isCreated());

        mockMvc.perform(authenticated(get("/pets/" + petId + "/history"), tutor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pet.id").value(petId))
                .andExpect(jsonPath("$.vaccines.length()").value(1))
                .andExpect(jsonPath("$.routines.length()").value(1))
                .andExpect(jsonPath("$.alerts.length()").value(1));
    }

    @Test
    @DisplayName("Histórico de pet inexistente retorna 404")
    void historyOfUnknownPetIsNotFound() throws Exception {
        String tutor = registerAndGetToken(Role.TUTOR);

        mockMvc.perform(authenticated(get("/pets/999999/history"), tutor))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Pet inativado deixa de ser acessível em todos os fluxos")
    void inactivePetIsNotReachable() throws Exception {
        String tutor = registerAndGetToken(Role.TUTOR);
        Long petId = createPet(tutor, "Thor");

        mockMvc.perform(authenticated(delete("/pets/" + petId), tutor))
                .andExpect(status().isNoContent());

        mockMvc.perform(authenticated(get("/pets/" + petId), tutor)).andExpect(status().isNotFound());
        mockMvc.perform(authenticated(get("/pets/" + petId + "/history"), tutor)).andExpect(status().isNotFound());
        mockMvc.perform(authenticated(get("/pets/" + petId + "/vaccines"), tutor)).andExpect(status().isNotFound());
        mockMvc.perform(authenticated(get("/pets/" + petId + "/routines"), tutor)).andExpect(status().isNotFound());
        mockMvc.perform(authenticated(get("/pets/" + petId + "/alerts"), tutor)).andExpect(status().isNotFound());
        mockMvc.perform(authenticated(put("/pets/" + petId), tutor)
                        .content(json(Map.of("name", "Renomeado", "species", "DOG", "tutorName", "Tutor"))))
                .andExpect(status().isNotFound());
        mockMvc.perform(authenticated(delete("/pets/" + petId), tutor)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Pet inativado sai das listagens do tutor")
    void inactivePetLeavesListings() throws Exception {
        String tutor = registerAndGetToken(Role.TUTOR);
        Long petId = createPet(tutor, "Thor");

        mockMvc.perform(authenticated(get("/pets"), tutor))
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(authenticated(delete("/pets/" + petId), tutor))
                .andExpect(status().isNoContent());

        mockMvc.perform(authenticated(get("/pets"), tutor))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @DisplayName("Registro de rotina com data futura é rejeitado")
    void futureRoutineIsRejected() throws Exception {
        String tutor = registerAndGetToken(Role.TUTOR);
        Long petId = createPet(tutor, "Thor");

        mockMvc.perform(authenticated(post("/routines"), tutor)
                        .content(json(Map.of(
                                "petId", petId,
                                "type", "WALK",
                                "recordDate", LocalDate.now().plusYears(5).toString()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.recordDate").exists());
    }
}

