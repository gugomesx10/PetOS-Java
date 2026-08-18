package br.com.petos.project.support;

import br.com.petos.project.enums.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Base dos testes de integracao web: sobe o contexto no perfil de teste
 * e oferece atalhos para autenticar usuarios reais via API.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    protected static final String DEFAULT_PASSWORD = "senhaSegura123";

    private static final AtomicInteger EMAIL_SEQUENCE = new AtomicInteger();

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    /**
     * Cadastra um usuario novo e devolve o token JWT emitido no cadastro.
     */
    protected String registerAndGetToken(Role role) throws Exception {
        String email = "user%d@petos.test".formatted(EMAIL_SEQUENCE.incrementAndGet());
        MvcResult result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", "Usuário de Teste",
                                "email", email,
                                "password", DEFAULT_PASSWORD,
                                "role", role.name()))))
                .andReturn();
        return readField(result, "token");
    }

    protected Long createPet(String token, String name) throws Exception {
        MvcResult result = mockMvc.perform(authenticated(post("/pets"), token)
                        .content(json(Map.of(
                                "name", name,
                                "species", "DOG",
                                "tutorName", "Tutor de Teste"))))
                .andReturn();
        return Long.valueOf(readField(result, "id"));
    }

    protected MockHttpServletRequestBuilder authenticated(MockHttpServletRequestBuilder builder, String token) {
        return builder.header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON);
    }

    protected String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    protected String readField(MvcResult result, String field) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString()).get(field).asText();
    }
}

