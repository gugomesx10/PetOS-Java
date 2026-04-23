package br.com.petos.project.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI petosOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PetOS API")
                        .description("API REST do sistema PetOS — plataforma de cuidado contínuo para pets. " +
                                "Permite cadastro de pets, controle de vacinas, rotinas e alertas automáticos.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("PetOS Team")
                                .url("https://github.com/gugomesx10/PetOS-Java"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}

