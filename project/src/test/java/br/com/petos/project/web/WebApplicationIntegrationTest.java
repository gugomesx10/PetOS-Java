package br.com.petos.project.web;

import br.com.petos.project.enums.Role;
import br.com.petos.project.repository.AlertRepository;
import br.com.petos.project.repository.VaccineRepository;
import br.com.petos.project.support.AbstractIntegrationTest;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class WebApplicationIntegrationTest extends AbstractIntegrationTest {
    @Autowired VaccineRepository vaccines;
    @Autowired AlertRepository alerts;
    @Autowired Flyway flyway;

    private record Account(String token, MockHttpSession session) { }

    private Account account(Role role) throws Exception {
        String token = registerAndGetToken(role);
        var me = mockMvc.perform(authenticated(get("/auth/me"), token)).andReturn();
        String email = readField(me, "email");
        var login = mockMvc.perform(post("/login").with(csrf()).param("email", email).param("password", DEFAULT_PASSWORD))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/web")).andReturn();
        var session = (MockHttpSession) login.getRequest().getSession(false);
        assertThat(session).isNotNull();
        assertThat(session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY)).isNotNull();
        return new Account(token, session);
    }

    @Test
    void publicPagesAndLocalAssetsRenderWithoutLogin() throws Exception {
        mockMvc.perform(get("/login")).andExpect(status().isOk()).andExpect(view().name("login"))
                .andExpect(content().string(containsString("name=\"_csrf\"")));
        mockMvc.perform(get("/cadastro")).andExpect(status().isOk()).andExpect(view().name("cadastro"));
        for (String path : new String[]{"/css/petos.css", "/images/companions.svg", "/images/paw.svg", "/js/forms.js"}) {
            mockMvc.perform(get(path)).andExpect(status().isOk());
        }
    }

    @Test
    void anonymousWebRedirectsButApiKeepsJson401() throws Exception {
        mockMvc.perform(get("/web")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrlPattern("**/login"));
        mockMvc.perform(get("/pets")).andExpect(status().isUnauthorized());
    }

    @Test
    void sessionAndBearerAuthenticationAreIsolated() throws Exception {
        var tutor = account(Role.TUTOR);
        mockMvc.perform(get("/pets").session(tutor.session())).andExpect(status().isUnauthorized());
        mockMvc.perform(authenticated(get("/web"), tutor.token())).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void bothRolesRenderHomeWithDifferentActions() throws Exception {
        var tutor = account(Role.TUTOR);
        var clinic = account(Role.CLINICA);
        mockMvc.perform(get("/web").session(tutor.session())).andExpect(status().isOk())
                .andExpect(content().string(containsString("Cadastrar pet")));
        mockMvc.perform(get("/web").session(clinic.session())).andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Cadastrar pet"))));
        mockMvc.perform(get("/web/pets/novo").session(clinic.session())).andExpect(status().isForbidden());
        mockMvc.perform(post("/web/pets/novo").session(clinic.session()).with(csrf())).andExpect(status().isForbidden());
    }

    @Test
    void invalidLoginDoesNotExposeCredentials() throws Exception {
        mockMvc.perform(post("/login").with(csrf()).param("email", "unknown@petos.test").param("password", "private-value"))
                .andExpect(redirectedUrl("/login?error"));
        mockMvc.perform(get("/login?error")).andExpect(status().isOk())
                .andExpect(content().string(containsString("E-mail ou senha inválidos")))
                .andExpect(content().string(not(containsString("private-value"))));
    }

    @Test
    void csrfRequiredOnLoginRegistrationAndWrites() throws Exception {
        mockMvc.perform(post("/login").param("email", "user@test.com").param("password", "password"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/cadastro")).andExpect(status().isForbidden());
        var tutor = account(Role.TUTOR);
        mockMvc.perform(post("/web/pets/novo").session(tutor.session())).andExpect(status().isForbidden());
        mockMvc.perform(post("/logout").session(tutor.session())).andExpect(status().isForbidden());
    }

    @Test
    void logoutInvalidatesSession() throws Exception {
        var tutor = account(Role.TUTOR);
        mockMvc.perform(post("/logout").session(tutor.session()).with(csrf())).andExpect(redirectedUrl("/login?logout"));
        assertThat(tutor.session().isInvalid()).isTrue();
        mockMvc.perform(get("/web")).andExpect(status().is3xxRedirection());
    }

    @Test
    void registrationReusesExistingAccountsAndValidation() throws Exception {
        String email = UUID.randomUUID() + "@petos.test";
        mockMvc.perform(post("/cadastro").with(csrf()).param("name", "Ana Tutor").param("email", email)
                        .param("password", DEFAULT_PASSWORD).param("role", "TUTOR"))
                .andExpect(redirectedUrl("/login"));
        mockMvc.perform(post("/login").with(csrf()).param("email", email).param("password", DEFAULT_PASSWORD))
                .andExpect(redirectedUrl("/web"));
        mockMvc.perform(post("/cadastro").with(csrf()).param("name", "Ana Tutor").param("email", email)
                        .param("password", DEFAULT_PASSWORD).param("role", "TUTOR"))
                .andExpect(status().isUnprocessableEntity()).andExpect(model().hasErrors());
        mockMvc.perform(post("/cadastro").with(csrf()).param("email", "invalid").param("password", "short"))
                .andExpect(status().isBadRequest()).andExpect(model().attributeHasFieldErrors("form", "name", "email", "password", "role"))
                .andExpect(content().string(not(containsString("value=\"short\""))));
    }

    @Test
    void petFormValidatesAndCreatesOwnedPetWithoutTrustingExtraFields() throws Exception {
        var tutor = account(Role.TUTOR);
        mockMvc.perform(get("/web/pets/novo").session(tutor.session())).andExpect(status().isOk());
        mockMvc.perform(post("/web/pets/novo").session(tutor.session()).with(csrf()).param("name", "X")
                        .param("weight", "-2").param("birthDate", LocalDate.now().plusDays(1).toString()))
                .andExpect(status().isBadRequest()).andExpect(model().attributeHasFieldErrors("form", "name", "weight", "birthDate"));
        var saved = mockMvc.perform(post("/web/pets/novo").session(tutor.session()).with(csrf()).param("name", "Amora Web")
                        .param("species", "CAT").param("tutorName", "Ana").param("ownerId", "9999")
                        .param("birthDate", "2020-01-15").param("weight", "4.2"))
                .andExpect(status().is3xxRedirection()).andReturn();
        mockMvc.perform(get(saved.getResponse().getRedirectedUrl()).session(tutor.session()))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Amora Web")));
    }

    @Test
    void vaccinationJourneyGeneratesSingleAlertAndConsolidatesHistory() throws Exception {
        var tutor = account(Role.TUTOR);
        var clinic = account(Role.CLINICA);
        Long pet = createPet(tutor.token(), "Luna Preventiva");
        String base = "/web/pets/" + pet;
        mockMvc.perform(get(base + "/vacinas/nova").session(clinic.session())).andExpect(status().isOk());
        mockMvc.perform(post(base + "/vacinas/nova").session(clinic.session()).with(csrf())
                        .param("name", "Antirrábica").param("dueDate", LocalDate.now().plusDays(5).toString()).param("petId", "99999"))
                .andExpect(redirectedUrl(base + "/vacinas"));
        var vaccine = vaccines.findByPetIdOrderByDueDateAsc(pet).get(0);
        assertThat(alerts.findByPetIdOrderByCreatedAtDesc(pet)).hasSize(1);
        mockMvc.perform(get(base + "/vacinas").session(tutor.session())).andExpect(status().isOk())
                .andExpect(content().string(containsString("Antirrábica")))
                .andExpect(content().string(containsString("Vence em breve")))
                .andExpect(content().string(not(containsString("Registrar vacina</a>"))));
        mockMvc.perform(get(base + "/vacinas/" + vaccine.getId() + "/editar").session(clinic.session()))
                .andExpect(status().isOk())
                .andExpect(result -> assertThat(org.springframework.web.util.HtmlUtils.htmlUnescape(
                        result.getResponse().getContentAsString()))
                        .contains("value=\"Antirrábica\"")
                        .contains("action=\"" + base + "/vacinas/" + vaccine.getId() + "/editar\""));
        mockMvc.perform(post(base + "/vacinas/" + vaccine.getId() + "/editar").session(clinic.session()).with(csrf())
                        .param("name", "Antirrábica").param("dueDate", LocalDate.now().plusDays(6).toString()))
                .andExpect(status().is3xxRedirection());
        assertThat(alerts.findByPetIdOrderByCreatedAtDesc(pet)).hasSize(1);
        mockMvc.perform(get(base + "/historico").session(tutor.session())).andExpect(status().isOk())
                .andExpect(content().string(containsString("Antirrábica"))).andExpect(content().string(containsString("Vacina próxima do vencimento")));
    }

    @Test
    void applicationResolvesPreventiveAlertAndClinicCanDelete() throws Exception {
        var tutor = account(Role.TUTOR);
        var clinic = account(Role.CLINICA);
        Long pet = createPet(tutor.token(), "Bob");
        String base = "/web/pets/" + pet + "/vacinas";
        mockMvc.perform(post(base + "/nova").session(clinic.session()).with(csrf()).param("name", "V10")
                        .param("dueDate", LocalDate.now().toString())).andExpect(status().is3xxRedirection());
        Long vaccine = vaccines.findByPetIdOrderByDueDateAsc(pet).get(0).getId();
        mockMvc.perform(post(base + "/" + vaccine + "/editar").session(clinic.session()).with(csrf())
                        .param("name", "V10").param("applicationDate", LocalDate.now().toString()))
                .andExpect(status().is3xxRedirection());
        assertThat(alerts.findByPetIdAndSentFalseOrderByDueDateAsc(pet)).isEmpty();
        mockMvc.perform(post(base + "/" + vaccine + "/excluir").session(clinic.session()).with(csrf()))
                .andExpect(status().is3xxRedirection());
        assertThat(vaccines.findById(vaccine)).isEmpty();
    }

    @Test
    void tutorCannotWriteVaccinesEvenByPostingDirectly() throws Exception {
        var tutor = account(Role.TUTOR);
        Long pet = createPet(tutor.token(), "Thor");
        String base = "/web/pets/" + pet + "/vacinas";
        mockMvc.perform(get(base).session(tutor.session())).andExpect(status().isOk());
        for (String action : new String[]{"/nova", "/1/editar", "/1/excluir"}) {
            mockMvc.perform(post(base + action).session(tutor.session()).with(csrf())).andExpect(status().isForbidden());
        }
        mockMvc.perform(get(base + "/nova").session(tutor.session())).andExpect(status().isForbidden());
    }

    @Test
    void ownershipAndInactivePetsAreRespectedInBothFlows() throws Exception {
        var owner = account(Role.TUTOR);
        var other = account(Role.TUTOR);
        var clinic = account(Role.CLINICA);
        Long pet = createPet(owner.token(), "Privado");
        for (String page : new String[]{"/vacinas", "/historico", "/rotinas/nova"}) {
            mockMvc.perform(get("/web/pets/" + pet + page).session(other.session())).andExpect(status().isForbidden())
                    .andExpect(view().name("error"));
        }
        mockMvc.perform(post("/web/pets/" + pet + "/rotinas/nova").session(other.session()).with(csrf()))
                .andExpect(status().isForbidden());
        mockMvc.perform(authenticated(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/pets/" + pet), owner.token()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/web/pets/" + pet + "/historico").session(clinic.session())).andExpect(status().isNotFound());
        mockMvc.perform(post("/web/pets/" + pet + "/vacinas/nova").session(clinic.session()).with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void vaccineAndRoutineDatesAreValidatedServerSide() throws Exception {
        var tutor = account(Role.TUTOR);
        var clinic = account(Role.CLINICA);
        Long pet = createPet(tutor.token(), "Datas");
        String base = "/web/pets/" + pet;
        mockMvc.perform(post(base + "/vacinas/nova").session(clinic.session()).with(csrf())
                        .param("name", "V10").param("applicationDate", LocalDate.now().plusDays(1).toString()))
                .andExpect(status().isBadRequest()).andExpect(model().attributeHasFieldErrors("form", "applicationDate"));
        mockMvc.perform(post(base + "/vacinas/nova").session(clinic.session()).with(csrf())
                        .param("name", "V10").param("applicationDate", "2020-02-02").param("dueDate", "2020-02-01"))
                .andExpect(status().isUnprocessableEntity()).andExpect(model().hasErrors());
        mockMvc.perform(post(base + "/rotinas/nova").session(tutor.session()).with(csrf())
                        .param("type", "WALK").param("recordDate", LocalDate.now().plusDays(1).toString()))
                .andExpect(status().isBadRequest()).andExpect(model().attributeHasFieldErrors("form", "recordDate"));
        mockMvc.perform(post(base + "/rotinas/nova").session(tutor.session()).with(csrf())
                        .param("type", "INVALID").param("recordDate", "invalid"))
                .andExpect(status().isBadRequest()).andExpect(model().attributeHasFieldErrors("form", "type", "recordDate"));
    }

    @Test
    void routineJourneyRefreshesRealHistoryAndEscapesUserContent() throws Exception {
        var tutor = account(Role.TUTOR);
        Long pet = createPet(tutor.token(), "História");
        String base = "/web/pets/" + pet;
        mockMvc.perform(get(base + "/historico").session(tutor.session())).andExpect(status().isOk())
                .andExpect(content().string(containsString("Nenhum registro nesta seleção")));
        mockMvc.perform(get(base + "/rotinas/nova").session(tutor.session())).andExpect(status().isOk());
        mockMvc.perform(post(base + "/rotinas/nova").session(tutor.session()).with(csrf()).param("type", "VET_VISIT")
                        .param("recordDate", LocalDate.now().toString()).param("description", "<script>alert(1)</script>").param("petId", "99999"))
                .andExpect(redirectedUrl(base + "/historico"));
        mockMvc.perform(get(base + "/historico").session(tutor.session())).andExpect(status().isOk())
                .andExpect(content().string(containsString("Consulta veterinária")))
                .andExpect(content().string(containsString("&lt;script&gt;")))
                .andExpect(content().string(not(containsString("<script>alert(1)</script>"))));
        mockMvc.perform(get(base + "/historico?category=Vacinas").session(tutor.session()))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Nenhum registro nesta seleção")));
    }

    @Test
    void invalidPetIdAndMissingPetProduceHtmlErrors() throws Exception {
        var tutor = account(Role.TUTOR);
        mockMvc.perform(get("/web/pets/abc/historico").session(tutor.session())).andExpect(status().isBadRequest()).andExpect(view().name("error"));
        mockMvc.perform(get("/web/pets/99999999/historico").session(tutor.session())).andExpect(status().isNotFound()).andExpect(view().name("error"));
    }

    @Test
    void flywayValidatesAndSecondMigrationDoesNotRecreateSchema() {
        assertThat(flyway.validateWithResult().validationSuccessful).isTrue();
        assertThat(flyway.info().applied()).hasSize(3);
        assertThat(flyway.migrate().migrationsExecuted).isZero();
    }
}


