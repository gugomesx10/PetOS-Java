package br.com.petos.project.dto;

import br.com.petos.project.enums.Species;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetResponseDTO {

    private Long id;
    private String name;
    private Species species;
    private String breed;
    private LocalDate birthDate;
    private Double weight;
    private String tutorName;
    private String tutorPhone;
    private Boolean active;
    private Integer ageInMonths;
}

