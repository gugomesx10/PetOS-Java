package br.com.petos.project.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Tratamento global de erros de entrada do cliente")
class GlobalExceptionHandlerIntegrationTest {

    private static final String MALFORMED_JSON = "{ isso nao e json }";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Enum invalido em path variable deve retornar 400 com os valores aceitos")
    void shouldReturnBadRequestWhenEnumInPathIsInvalid() throws Exception {
        mockMvc.perform(get("/pets/species/UNICORN"))
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
    @DisplayName("Long invalido em path variable deve retornar 400 informando o tipo esperado")
    void shouldReturnBadRequestWhenIdInPathIsNotANumber() throws Exception {
        mockMvc.perform(get("/pets/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/pets/abc"))
                .andExpect(jsonPath("$.message", containsString("id")))
                .andExpect(jsonPath("$.message", containsString("abc")))
                .andExpect(jsonPath("$.message", containsString("Long")));
    }

    @Test
    @DisplayName("Propriedade de ordenacao inexistente deve retornar 400")
    void shouldReturnBadRequestWhenSortPropertyDoesNotExist() throws Exception {
        mockMvc.perform(get("/pets").param("sort", "naoExiste"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/pets"))
                .andExpect(jsonPath("$.message", containsString("naoExiste")))
                .andExpect(jsonPath("$.message", containsString("sort")));
    }

    @Test
    @DisplayName("Enum invalido no corpo JSON deve retornar 400 com o campo e os valores aceitos")
    void shouldReturnBadRequestWhenEnumInBodyIsInvalid() throws Exception {
        String body = """
                {"petId":1,"type":"NOT_A_TYPE","message":"Mensagem de teste"}
                """;

        mockMvc.perform(post("/alerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/alerts"))
                .andExpect(jsonPath("$.message", containsString("type")))
                .andExpect(jsonPath("$.message", containsString("NOT_A_TYPE")))
                .andExpect(jsonPath("$.message", containsString("VACCINE_DUE")));
    }

    @Test
    @DisplayName("JSON mal formado deve retornar 400 sem expor detalhes internos")
    void shouldReturnBadRequestWhenBodyIsMalformedJson() throws Exception {
        mockMvc.perform(post("/alerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MALFORMED_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Corpo da requisição inválido ou mal formatado."));
    }

    @Test
    @DisplayName("Requisicoes validas nao devem ser afetadas pelos novos handlers")
    void shouldKeepValidRequestsWorking() throws Exception {
        mockMvc.perform(get("/pets/species/DOG"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/pets").param("sort", "name"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Recurso inexistente deve continuar retornando 404")
    void shouldKeepReturningNotFoundForUnknownResource() throws Exception {
        mockMvc.perform(get("/pets/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("Corpo invalido por Bean Validation deve continuar retornando 400 com fieldErrors")
    void shouldKeepReturningValidationErrors() throws Exception {
        mockMvc.perform(post("/pets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"A\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.fieldErrors").exists());
    }
}



