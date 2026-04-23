package br.com.petos.project.service;

import br.com.petos.project.dto.VaccineRequestDTO;
import br.com.petos.project.dto.VaccineResponseDTO;
import br.com.petos.project.entity.Alert;
import br.com.petos.project.entity.Pet;
import br.com.petos.project.entity.Vaccine;
import br.com.petos.project.enums.AlertType;
import br.com.petos.project.enums.VaccineStatus;
import br.com.petos.project.exception.ResourceNotFoundException;
import br.com.petos.project.mapper.VaccineMapper;
import br.com.petos.project.repository.AlertRepository;
import br.com.petos.project.repository.VaccineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VaccineService {

    private final VaccineRepository vaccineRepository;
    private final AlertRepository alertRepository;
    private final PetService petService;
    private final VaccineMapper vaccineMapper;

    @Transactional(readOnly = true)
    public Page<VaccineResponseDTO> findAll(Pageable pageable) {
        return vaccineRepository.findAll(pageable).map(vaccineMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public VaccineResponseDTO findById(Long id) {
        return vaccineMapper.toResponseDTO(findVaccineById(id));
    }

    @Transactional(readOnly = true)
    public List<VaccineResponseDTO> findByPetId(Long petId) {
        petService.findPetById(petId);
        return vaccineRepository.findByPetId(petId)
                .stream().map(vaccineMapper::toResponseDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<VaccineResponseDTO> findPendingByPetId(Long petId) {
        petService.findPetById(petId);
        return vaccineRepository.findPendingByPetId(petId)
                .stream().map(vaccineMapper::toResponseDTO).toList();
    }

    @Transactional
    public VaccineResponseDTO create(VaccineRequestDTO dto) {
        Pet pet = petService.findPetById(dto.getPetId());
        Vaccine vaccine = vaccineMapper.toEntity(dto, pet);

        // Regra de negócio: atualizar status automaticamente
        vaccine = updateVaccineStatus(vaccine);
        vaccine = vaccineRepository.save(vaccine);

        // Regra de negócio: gerar alerta automático se vacina tiver vencimento futuro
        if (dto.getDueDate() != null && dto.getDueDate().isAfter(LocalDate.now())) {
            generateVaccineAlert(pet, vaccine);
        }

        return vaccineMapper.toResponseDTO(vaccine);
    }

    @Transactional
    public VaccineResponseDTO update(Long id, VaccineRequestDTO dto) {
        Vaccine vaccine = findVaccineById(id);
        vaccineMapper.updateEntity(vaccine, dto);
        vaccine = updateVaccineStatus(vaccine);
        return vaccineMapper.toResponseDTO(vaccineRepository.save(vaccine));
    }

    @Transactional
    public void delete(Long id) {
        Vaccine vaccine = findVaccineById(id);
        vaccineRepository.delete(vaccine);
    }

    private Vaccine updateVaccineStatus(Vaccine vaccine) {
        if (vaccine.getDueDate() == null) return vaccine;

        LocalDate today = LocalDate.now();
        if (vaccine.getApplicationDate() != null && !vaccine.getApplicationDate().isAfter(today)) {
            if (vaccine.getDueDate().isBefore(today)) {
                vaccine.setStatus(VaccineStatus.OVERDUE);
            } else if (vaccine.getDueDate().isBefore(today.plusDays(30))) {
                vaccine.setStatus(VaccineStatus.EXPIRING_SOON);
            } else {
                vaccine.setStatus(VaccineStatus.APPLIED);
            }
        } else if (vaccine.getDueDate().isBefore(today)) {
            vaccine.setStatus(VaccineStatus.OVERDUE);
        } else if (vaccine.getDueDate().isBefore(today.plusDays(30))) {
            vaccine.setStatus(VaccineStatus.EXPIRING_SOON);
        }

        return vaccine;
    }

    private void generateVaccineAlert(Pet pet, Vaccine vaccine) {
        String message = String.format(
                "Vacina '%s' do pet %s vence em %s. Agende a dose de reforço!",
                vaccine.getName(), pet.getName(), vaccine.getDueDate()
        );
        Alert alert = Alert.builder()
                .pet(pet)
                .type(AlertType.VACCINE_DUE)
                .message(message)
                .dueDate(vaccine.getDueDate())
                .sent(false)
                .createdAt(LocalDateTime.now())
                .build();
        alertRepository.save(alert);
    }

    private Vaccine findVaccineById(Long id) {
        return vaccineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vacina", id));
    }
}

