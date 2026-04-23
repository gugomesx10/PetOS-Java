package br.com.petos.project.mapper;

import br.com.petos.project.dto.AlertRequestDTO;
import br.com.petos.project.dto.AlertResponseDTO;
import br.com.petos.project.entity.Alert;
import br.com.petos.project.entity.Pet;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AlertMapper {

    public Alert toEntity(AlertRequestDTO dto, Pet pet) {
        return Alert.builder()
                .pet(pet)
                .type(dto.getType())
                .message(dto.getMessage())
                .dueDate(dto.getDueDate())
                .sent(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public AlertResponseDTO toResponseDTO(Alert alert) {
        return AlertResponseDTO.builder()
                .id(alert.getId())
                .petId(alert.getPet().getId())
                .petName(alert.getPet().getName())
                .type(alert.getType())
                .message(alert.getMessage())
                .dueDate(alert.getDueDate())
                .sent(alert.getSent())
                .createdAt(alert.getCreatedAt())
                .build();
    }

    public void updateEntity(Alert alert, AlertRequestDTO dto) {
        alert.setType(dto.getType());
        alert.setMessage(dto.getMessage());
        alert.setDueDate(dto.getDueDate());
    }
}

