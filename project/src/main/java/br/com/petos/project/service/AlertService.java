package br.com.petos.project.service;

import br.com.petos.project.dto.AlertRequestDTO;
import br.com.petos.project.dto.AlertResponseDTO;
import br.com.petos.project.entity.Alert;
import br.com.petos.project.entity.Pet;
import br.com.petos.project.exception.ResourceNotFoundException;
import br.com.petos.project.mapper.AlertMapper;
import br.com.petos.project.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;
    private final PetService petService;
    private final AlertMapper alertMapper;

    @Transactional(readOnly = true)
    public Page<AlertResponseDTO> findAll(Pageable pageable) {
        return alertRepository.findAll(pageable).map(alertMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public AlertResponseDTO findById(Long id) {
        return alertMapper.toResponseDTO(findAlertById(id));
    }

    @Transactional(readOnly = true)
    public List<AlertResponseDTO> findByPetId(Long petId) {
        petService.findPetById(petId);
        return alertRepository.findByPetIdOrderByCreatedAtDesc(petId)
                .stream().map(alertMapper::toResponseDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<AlertResponseDTO> findPendingByPetId(Long petId) {
        petService.findPetById(petId);
        return alertRepository.findByPetIdAndSentFalseOrderByDueDateAsc(petId)
                .stream().map(alertMapper::toResponseDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<AlertResponseDTO> findAllPending() {
        return alertRepository.findBySentFalseOrderByDueDateAsc()
                .stream().map(alertMapper::toResponseDTO).toList();
    }

    @Transactional
    public AlertResponseDTO create(AlertRequestDTO dto) {
        Pet pet = petService.findPetById(dto.getPetId());
        Alert alert = alertMapper.toEntity(dto, pet);
        return alertMapper.toResponseDTO(alertRepository.save(alert));
    }

    @Transactional
    public AlertResponseDTO update(Long id, AlertRequestDTO dto) {
        Alert alert = findAlertById(id);
        alertMapper.updateEntity(alert, dto);
        return alertMapper.toResponseDTO(alertRepository.save(alert));
    }

    @Transactional
    public AlertResponseDTO markAsSent(Long id) {
        Alert alert = findAlertById(id);
        alert.setSent(true);
        return alertMapper.toResponseDTO(alertRepository.save(alert));
    }

    @Transactional
    public void delete(Long id) {
        Alert alert = findAlertById(id);
        alertRepository.delete(alert);
    }

    private Alert findAlertById(Long id) {
        return alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta", id));
    }
}

