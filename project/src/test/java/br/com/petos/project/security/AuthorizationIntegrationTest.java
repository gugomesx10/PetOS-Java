package br.com.petos.project.security;

import br.com.petos.project.enums.Role;
import br.com.petos.project.support.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Autorização: propriedade do tutor e perfis TUTOR × CLINICA")
class AuthorizationIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("Tutor acessa o próprio pet")
    void ownerReadsOwnPet() throws Exception {
        String tutor = registerAndGetToken(Role.TUTOR);
        Long petId = createPet(tutor, "Thor");

        mockMvc.perform(authenticated(get("/pets/" + petId), tutor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Thor"));
    }

    @Test
    @DisplayName("Trocar o ID na URL não dá acesso ao pet de outro tutor")
    void otherTutorCannotReadPet() throws Exception {
        String dono = registerAndGetToken(Role.TUTOR);
        String invasor = registerAndGetToken(Role.TUTOR);
        Long petId = createPet(dono, "Luna");

        mockMvc.perform(authenticated(get("/pets/" + petId), invasor))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("Outro tutor não altera nem inativa pet alheio")
    void otherTutorCannotWritePet() throws Exception {
        String dono = registerAndGetToken(Role.TUTOR);
        String invasor = registerAndGetToken(Role.TUTOR);
        Long petId = createPet(dono, "Bob");

        mockMvc.perform(authenticated(put("/pets/" + petId), invasor)
                        .content(json(Map.of("name", "Sequestrado", "species", "DOG", "tutorName", "Invasor"))))
                .andExpect(status().isForbidden());

        mockMvc.perform(authenticated(delete("/pets/" + petId), invasor))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Listagem do tutor não inclui pets de outro tutor")
    void listingIsScopedToOwner() throws Exception {
        String dono = registerAndGetToken(Role.TUTOR);
        String outro = registerAndGetToken(Role.TUTOR);
        createPet(dono, "Somente Meu");

        mockMvc.perform(authenticated(get("/pets"), outro))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @DisplayName("Histórico de pet alheio é negado")
    void otherTutorCannotReadHistory() throws Exception {
        String dono = registerAndGetToken(Role.TUTOR);
        String invasor = registerAndGetToken(Role.TUTOR);
        Long petId = createPet(dono, "Nina");

        mockMvc.perform(authenticated(get("/pets/" + petId + "/history"), invasor))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("CLINICA acessa o pet de qualquer tutor para fins clínicos")
    void clinicReadsAnyPet() throws Exception {
        String tutor = registerAndGetToken(Role.TUTOR);
        String clinica = registerAndGetToken(Role.CLINICA);
        Long petId = createPet(tutor, "Thor");

        mockMvc.perform(authenticated(get("/pets/" + petId), clinica))
                .andExpect(status().isOk());
        mockMvc.perform(authenticated(get("/pets/" + petId + "/history"), clinica))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("CLINICA não cadastra pet: o pet pertence ao tutor")
    void clinicCannotCreatePet() throws Exception {
        String clinica = registerAndGetToken(Role.CLINICA);

        mockMvc.perform(authenticated(post("/pets"), clinica)
                        .content(json(Map.of("name", "Sem Dono", "species", "CAT", "tutorName", "Clínica"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("TUTOR não registra vacina: ato clínico é da CLINICA")
    void tutorCannotRegisterVaccine() throws Exception {
        String tutor = registerAndGetToken(Role.TUTOR);
        Long petId = createPet(tutor, "Thor");

        mockMvc.perform(authenticated(post("/vaccines"), tutor)
                        .content(json(Map.of("petId", petId, "name", "V10"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("TUTOR não cria nem remove alerta manualmente")
    void tutorCannotManageAlerts() throws Exception {
        String tutor = registerAndGetToken(Role.TUTOR);
        Long petId = createPet(tutor, "Thor");

        mockMvc.perform(authenticated(post("/alerts"), tutor)
                        .content(json(Map.of("petId", petId, "type", "HEALTH_CHECK", "message", "Teste"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("TUTOR registra rotina do próprio pet")
    void tutorManagesOwnRoutines() throws Exception {
        String tutor = registerAndGetToken(Role.TUTOR);
        Long petId = createPet(tutor, "Thor");

        mockMvc.perform(authenticated(post("/routines"), tutor)
                        .content(json(Map.of(
                                "petId", petId,
                                "type", "WALK",
                                "description", "Caminhada no parque",
                                "recordDate", java.time.LocalDate.now().toString()))))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("TUTOR não registra rotina em pet de outro tutor")
    void tutorCannotCreateRoutineForOtherPet() throws Exception {
        String dono = registerAndGetToken(Role.TUTOR);
        String invasor = registerAndGetToken(Role.TUTOR);
        Long petId = createPet(dono, "Thor");

        mockMvc.perform(authenticated(post("/routines"), invasor)
                        .content(json(Map.of(
                                "petId", petId,
                                "type", "WALK",
                                "recordDate", java.time.LocalDate.now().toString()))))
                .andExpect(status().isForbidden());
    }
}

