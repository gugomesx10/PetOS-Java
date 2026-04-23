package br.com.petos.project.dto;

import br.com.petos.project.enums.RoutineType;
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
public class RoutineRequestDTO {

    @NotNull(message = "ID do pet é obrigatório")
    private Long petId;

    @NotNull(message = "Tipo de rotina é obrigatório")
    private RoutineType type;

    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    private String description;

    @NotNull(message = "Data do registro é obrigatória")
    private LocalDate recordDate;
}

