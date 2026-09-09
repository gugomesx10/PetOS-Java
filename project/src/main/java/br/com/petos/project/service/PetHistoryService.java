package br.com.petos.project.service;

import br.com.petos.project.dto.PetHistoryDTO;
import br.com.petos.project.entity.Pet;
import br.com.petos.project.mapper.AlertMapper;
import br.com.petos.project.mapper.PetMapper;
import br.com.petos.project.mapper.RoutineRecordMapper;
import br.com.petos.project.mapper.VaccineMapper;
import br.com.petos.project.repository.AlertRepository;
import br.com.petos.project.repository.RoutineRecordRepository;
import br.com.petos.project.repository.VaccineRepository;
import br.com.petos.project.security.PetAccessPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PetHistoryService {

    private final VaccineRepository vaccineRepository;
    private final RoutineRecordRepository routineRecordRepository;
    private final AlertRepository alertRepository;
    private final PetMapper petMapper;
    private final VaccineMapper vaccineMapper;
    private final RoutineRecordMapper routineRecordMapper;
    private final AlertMapper alertMapper;
    private final PetAccessPolicy petAccessPolicy;

    @Transactional(readOnly = true)
    public PetHistoryDTO getHistory(Long petId) {
        Pet pet = petAccessPolicy.requireReadable(petId);

        return PetHistoryDTO.builder()
                .pet(petMapper.toResponseDTO(pet))
                .vaccines(vaccineRepository.findByPetIdOrderByDueDateAsc(pet.getId())
                        .stream().map(vaccineMapper::toResponseDTO).toList())
                .routines(routineRecordRepository.findByPetIdOrderByRecordDateDesc(pet.getId())
                        .stream().map(routineRecordMapper::toResponseDTO).toList())
                .alerts(alertRepository.findByPetIdOrderByCreatedAtDesc(pet.getId())
                        .stream().map(alertMapper::toResponseDTO).toList())
                .build();
    }
}

