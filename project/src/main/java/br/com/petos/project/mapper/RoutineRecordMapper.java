package br.com.petos.project.mapper;

import br.com.petos.project.dto.RoutineRequestDTO;
import br.com.petos.project.dto.RoutineResponseDTO;
import br.com.petos.project.entity.Pet;
import br.com.petos.project.entity.RoutineRecord;
import org.springframework.stereotype.Component;

@Component
public class RoutineRecordMapper {

    public RoutineRecord toEntity(RoutineRequestDTO dto, Pet pet) {
        return RoutineRecord.builder()
                .pet(pet)
                .type(dto.getType())
                .description(dto.getDescription())
                .recordDate(dto.getRecordDate())
                .build();
    }

    public RoutineResponseDTO toResponseDTO(RoutineRecord record) {
        return RoutineResponseDTO.builder()
                .id(record.getId())
                .petId(record.getPet().getId())
                .petName(record.getPet().getName())
                .type(record.getType())
                .description(record.getDescription())
                .recordDate(record.getRecordDate())
                .build();
    }

    public void updateEntity(RoutineRecord record, RoutineRequestDTO dto) {
        record.setType(dto.getType());
        record.setDescription(dto.getDescription());
        record.setRecordDate(dto.getRecordDate());
    }
}

