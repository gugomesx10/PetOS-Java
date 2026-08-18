package br.com.petos.project;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ProjectApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("O contexto da aplicacao PetOS deve carregar com sucesso")
    void contextLoads() {
        assertThat(applicationContext).isNotNull();
    }
}

