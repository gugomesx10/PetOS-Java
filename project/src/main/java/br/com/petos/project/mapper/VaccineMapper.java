package br.com.petos.project.mapper;

import br.com.petos.project.dto.VaccineRequestDTO;
import br.com.petos.project.dto.VaccineResponseDTO;
import br.com.petos.project.entity.Pet;
import br.com.petos.project.entity.Vaccine;
import br.com.petos.project.enums.VaccineStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class VaccineMapper {

    public Vaccine toEntity(VaccineRequestDTO dto, Pet pet) {
        VaccineStatus status = dto.getStatus() != null ? dto.getStatus() : VaccineStatus.PENDING;
        return Vaccine.builder()
                .pet(pet)
                .name(dto.getName())
                .applicationDate(dto.getApplicationDate())
                .dueDate(dto.getDueDate())
                .status(status)
                .build();
    }

    public VaccineResponseDTO toResponseDTO(Vaccine vaccine) {
        boolean expiringSoon = false;
        if (vaccine.getDueDate() != null && vaccine.getStatus() != VaccineStatus.APPLIED) {
            expiringSoon = vaccine.getDueDate().isBefore(LocalDate.now().plusDays(30));
        }
        return VaccineResponseDTO.builder()
                .id(vaccine.getId())
                .petId(vaccine.getPet().getId())
                .petName(vaccine.getPet().getName())
                .name(vaccine.getName())
                .applicationDate(vaccine.getApplicationDate())
                .dueDate(vaccine.getDueDate())
                .status(vaccine.getStatus())
                .expiringSoon(expiringSoon)
                .build();
    }

    public void updateEntity(Vaccine vaccine, VaccineRequestDTO dto) {
        vaccine.setName(dto.getName());
        vaccine.setApplicationDate(dto.getApplicationDate());
        vaccine.setDueDate(dto.getDueDate());
        if (dto.getStatus() != null) {
            vaccine.setStatus(dto.getStatus());
        }
    }
}

