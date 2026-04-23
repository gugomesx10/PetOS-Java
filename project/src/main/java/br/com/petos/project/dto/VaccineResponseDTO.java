package br.com.petos.project.dto;

import br.com.petos.project.enums.VaccineStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VaccineResponseDTO {

    private Long id;
    private Long petId;
    private String petName;
    private String name;
    private LocalDate applicationDate;
    private LocalDate dueDate;
    private VaccineStatus status;
    private Boolean expiringSoon;
}

