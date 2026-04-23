package br.com.petos.project.dto;

import br.com.petos.project.enums.Species;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetRequestDTO {

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    private String name;

    @NotNull(message = "Espécie é obrigatória")
    private Species species;

    @Size(max = 100, message = "Raça deve ter no máximo 100 caracteres")
    private String breed;

    @Past(message = "Data de nascimento deve ser no passado")
    private LocalDate birthDate;

    @Positive(message = "Peso deve ser positivo")
    private Double weight;

    @NotBlank(message = "Nome do tutor é obrigatório")
    @Size(max = 100, message = "Nome do tutor deve ter no máximo 100 caracteres")
    private String tutorName;

    @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
    private String tutorPhone;
}

