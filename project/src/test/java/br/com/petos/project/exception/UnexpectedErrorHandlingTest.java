package br.com.petos.project.exception;

import br.com.petos.project.enums.Role;
import br.com.petos.project.service.PetService;
import br.com.petos.project.support.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Tratamento global de erros inesperados do servidor")
class UnexpectedErrorHandlingTest extends AbstractIntegrationTest {

    private static final String INTERNAL_DETAIL = "detalhe-interno-confidencial-do-banco";

    @MockitoBean
    private PetService petService;

    @Test
    @DisplayName("Erro inesperado deve retornar 500 sem vazar detalhes internos nem stack trace")
    void shouldReturnInternalServerErrorWithoutLeakingDetails() throws Exception {
        String token = registerAndGetToken(Role.TUTOR);
        given(petService.findById(anyLong()))
                .willThrow(new IllegalStateException(INTERNAL_DETAIL));

        mockMvc.perform(authenticated(get("/pets/1"), token))
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
