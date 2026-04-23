package br.com.petos.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetHistoryDTO {

    private PetResponseDTO pet;
    private List<VaccineResponseDTO> vaccines;
    private List<RoutineResponseDTO> routines;
    private List<AlertResponseDTO> alerts;
}

