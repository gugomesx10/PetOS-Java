package br.com.petos.project.exception;

import br.com.petos.project.enums.Role;
import br.com.petos.project.support.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Tratamento global de erros de entrada do cliente")
class GlobalExceptionHandlerIntegrationTest extends AbstractIntegrationTest {

    private static final String MALFORMED_JSON = "{ isso nao e json }";

    private String token;

    @BeforeEach
    void authenticate() throws Exception {
        token = registerAndGetToken(Role.CLINICA);
    }

    @Test
    @DisplayName("Enum inválido em path variable deve retornar 400 com os valores aceitos")
    void shouldReturnBadRequestWhenEnumInPathIsInvalid() throws Exception {
        mockMvc.perform(authenticated(get("/pets/species/UNICORN"), token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/pets/species/UNICORN"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.message", containsString("species")))
                .andExpect(jsonPath("$.message", containsString("UNICORN")))
                .andExpect(jsonPath("$.message", containsString("DOG")));
    }

    @Test
    @DisplayName("Long inválido em path variable deve retornar 400 informando o tipo esperado")
    void shouldReturnBadRequestWhenIdInPathIsNotANumber() throws Exception {
        mockMvc.perform(authenticated(get("/pets/abc"), token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("id")))
                .andExpect(jsonPath("$.message", containsString("abc")))
                .andExpect(jsonPath("$.message", containsString("Long")));
    }

    @Test
    @DisplayName("Propriedade de ordenação inexistente deve retornar 400")
    void shouldReturnBadRequestWhenSortPropertyDoesNotExist() throws Exception {
        mockMvc.perform(authenticated(get("/pets"), token).param("sort", "naoExiste"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("naoExiste")))
                .andExpect(jsonPath("$.message", containsString("sort")));
    }

    @Test
    @DisplayName("Enum inválido no corpo JSON deve retornar 400 com o campo e os valores aceitos")
    void shouldReturnBadRequestWhenEnumInBodyIsInvalid() throws Exception {
        mockMvc.perform(authenticated(post("/alerts"), token)
                        .content("""
                                {"petId":1,"type":"NOT_A_TYPE","message":"Mensagem de teste"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("type")))
                .andExpect(jsonPath("$.message", containsString("NOT_A_TYPE")))
                .andExpect(jsonPath("$.message", containsString("VACCINE_DUE")));
    }

    @Test
    @DisplayName("JSON mal formado deve retornar 400 sem expor detalhes internos")
    void shouldReturnBadRequestWhenBodyIsMalformedJson() throws Exception {
        mockMvc.perform(authenticated(post("/alerts"), token).content(MALFORMED_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Corpo da requisição inválido ou mal formatado."));
    }

    @Test
    @DisplayName("Parâmetro obrigatório ausente deve retornar 400")
    void shouldReturnBadRequestWhenRequiredParameterIsMissing() throws Exception {
        mockMvc.perform(authenticated(get("/pets/search"), token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("name")));
    }

    @Test
    @DisplayName("Método HTTP não suportado deve retornar 405")
    void shouldReturnMethodNotAllowed() throws Exception {
        mockMvc.perform(authenticated(post("/pets/1"), token).content("{}"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.error").value("Method Not Allowed"));
    }

    @Test
    @DisplayName("Content-Type não suportado deve retornar 415")
    void shouldReturnUnsupportedMediaType() throws Exception {
        mockMvc.perform(post("/pets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("conteudo em texto puro"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415))
                .andExpect(jsonPath("$.error").value("Unsupported Media Type"));
    }

    @Test
    @DisplayName("Rota inexistente deve retornar 404")
    void shouldReturnNotFoundForUnknownRoute() throws Exception {
        mockMvc.perform(authenticated(get("/rota-inexistente"), token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("Requisições válidas não devem ser afetadas pelos handlers")
    void shouldKeepValidRequestsWorking() throws Exception {
        mockMvc.perform(authenticated(get("/pets/species/DOG"), token)).andExpect(status().isOk());
        mockMvc.perform(authenticated(get("/pets"), token).param("sort", "name")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Recurso inexistente deve continuar retornando 404")
    void shouldKeepReturningNotFoundForUnknownResource() throws Exception {
        mockMvc.perform(authenticated(get("/pets/999999"), token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("Corpo inválido por Bean Validation deve retornar 400 com fieldErrors")
    void shouldKeepReturningValidationErrors() throws Exception {
        String tutor = registerAndGetToken(Role.TUTOR);

        mockMvc.perform(authenticated(post("/pets"), tutor).content(json(Map.of("name", "A"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.fieldErrors").exists());
    }
}

