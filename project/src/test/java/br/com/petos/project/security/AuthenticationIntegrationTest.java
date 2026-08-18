package br.com.petos.project.security;

import br.com.petos.project.enums.Role;
import br.com.petos.project.support.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Cadastro, login e token JWT")
class AuthenticationIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("Cadastro retorna 201 com token e nunca devolve a senha")
    void registerReturnsToken() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", "Ana Tutora",
                                "email", "ana.tutora@petos.test",
                                "password", DEFAULT_PASSWORD,
                                "role", "TUTOR"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.role").value("TUTOR"))
                .andExpect(content().string(not(containsString(DEFAULT_PASSWORD))))
                .andExpect(content().string(not(containsString("passwordHash"))));
    }

    @Test
    @DisplayName("Email já cadastrado retorna 422")
    void duplicatedEmailIsRejected() throws Exception {
        String body = json(Map.of(
                "name", "Duplicado",
                "email", "duplicado@petos.test",
                "password", DEFAULT_PASSWORD,
                "role", "TUTOR"));

        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("Business Rule Violation"));
    }

    @Test
    @DisplayName("Cadastro com email inválido e senha curta retorna 400")
    void invalidRegistrationIsRejected() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", "X",
                                "email", "nao-e-email",
                                "password", "123",
                                "role", "TUTOR"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists());
    }

    @Test
    @DisplayName("Login com credenciais corretas retorna token")
    void loginSucceeds() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", "Login Ok",
                                "email", "login.ok@petos.test",
                                "password", DEFAULT_PASSWORD,
                                "role", "CLINICA"))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "login.ok@petos.test", "password", DEFAULT_PASSWORD))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.role").value("CLINICA"));
    }

    @Test
    @DisplayName("Login com senha incorreta retorna 401 sem revelar o motivo")
    void loginWithWrongPasswordIsUnauthorized() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", "Senha Errada",
                                "email", "senha.errada@petos.test",
                                "password", DEFAULT_PASSWORD,
                                "role", "TUTOR"))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "senha.errada@petos.test", "password", "outraSenha123"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Credenciais inválidas."));
    }

    @Test
    @DisplayName("Login de usuário inexistente retorna 401")
    void loginOfUnknownUserIsUnauthorized() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "nao.existe@petos.test", "password", DEFAULT_PASSWORD))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Endpoint protegido sem token retorna 401")
    void protectedEndpointWithoutTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get("/pets"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("Token inválido não autentica")
    void invalidTokenIsRejected() throws Exception {
        mockMvc.perform(get("/pets").header("Authorization", "Bearer token.invalido.aqui"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Usuário autenticado consulta os próprios dados em /auth/me")
    void currentUserIsExposed() throws Exception {
        String token = registerAndGetToken(Role.TUTOR);

        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("TUTOR"))
                .andExpect(jsonPath("$.email").exists());
    }
}

