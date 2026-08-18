package br.com.petos.project.exception;

import br.com.petos.project.service.PetService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Tratamento global de erros inesperados do servidor")
class UnexpectedErrorHandlingTest {

    private static final String INTERNAL_DETAIL = "detalhe-interno-confidencial-do-banco";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PetService petService;

    @Test
    @DisplayName("Erro inesperado deve retornar 500 sem vazar detalhes internos nem stack trace")
    void shouldReturnInternalServerErrorWithoutLeakingDetails() throws Exception {
        given(petService.findById(anyLong()))
                .willThrow(new IllegalStateException(INTERNAL_DETAIL));

        mockMvc.perform(get("/pets/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.path").value("/pets/1"))
                .andExpect(jsonPath("$.message").value("Ocorreu um erro inesperado. Tente novamente mais tarde."))
                .andExpect(content().string(not(containsString(INTERNAL_DETAIL))))
                .andExpect(content().string(not(containsString("IllegalStateException"))))
                .andExpect(content().string(not(containsString("br.com.petos"))));
    }
}

