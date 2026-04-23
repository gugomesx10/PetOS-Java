package br.com.petos.project.dto;

import br.com.petos.project.enums.VaccineStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VaccineRequestDTO {

    @NotNull(message = "ID do pet é obrigatório")
    private Long petId;

    @NotBlank(message = "Nome da vacina é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    private String name;

    private LocalDate applicationDate;

    private LocalDate dueDate;

    private VaccineStatus status;
}

