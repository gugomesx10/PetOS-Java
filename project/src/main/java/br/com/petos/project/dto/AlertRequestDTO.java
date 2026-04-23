package br.com.petos.project.dto;

import br.com.petos.project.enums.AlertType;
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
public class AlertRequestDTO {

    @NotNull(message = "ID do pet é obrigatório")
    private Long petId;

    @NotNull(message = "Tipo de alerta é obrigatório")
    private AlertType type;

    @NotBlank(message = "Mensagem é obrigatória")
    @Size(max = 500, message = "Mensagem deve ter no máximo 500 caracteres")
    private String message;

    private LocalDate dueDate;
}

