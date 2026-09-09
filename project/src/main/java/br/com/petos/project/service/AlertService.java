package br.com.petos.project.service;

import br.com.petos.project.dto.AlertRequestDTO;
import br.com.petos.project.dto.AlertResponseDTO;
import br.com.petos.project.entity.Alert;
import br.com.petos.project.entity.Pet;
import br.com.petos.project.exception.ResourceNotFoundException;
import br.com.petos.project.mapper.AlertMapper;
import br.com.petos.project.repository.AlertRepository;
import br.com.petos.project.security.AuthenticatedUser;
import br.com.petos.project.security.CurrentUserProvider;
import br.com.petos.project.security.PetAccessPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;
    private final AlertMapper alertMapper;
    private final PetAccessPolicy petAccessPolicy;
    private final CurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    public Page<AlertResponseDTO> findAll(Pageable pageable) {
        AuthenticatedUser user = currentUserProvider.require();
        Page<Alert> alerts = user.isClinica()
                ? alertRepository.findByPetActiveTrue(pageable)
                : alertRepository.findByPetOwnerIdAndPetActiveTrue(user.getId(), pageable);
        return alerts.map(alertMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public AlertResponseDTO findById(Long id) {
        Alert alert = findAlertById(id);
        petAccessPolicy.requireReadable(alert.getPet().getId());
        return alertMapper.toResponseDTO(alert);
    }

    @Transactional(readOnly = true)
    public List<AlertResponseDTO> findByPetId(Long petId) {
        petAccessPolicy.requireReadable(petId);
        return alertRepository.findByPetIdOrderByCreatedAtDesc(petId)
                .stream().map(alertMapper::toResponseDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<AlertResponseDTO> findPendingByPetId(Long petId) {
        petAccessPolicy.requireReadable(petId);
        return alertRepository.findByPetIdAndSentFalseOrderByDueDateAsc(petId)
                .stream().map(alertMapper::toResponseDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<AlertResponseDTO> findAllPending() {
        AuthenticatedUser user = currentUserProvider.require();
        List<Alert> alerts = user.isClinica()
                ? alertRepository.findByPetActiveTrueAndSentFalseOrderByDueDateAsc()
                : alertRepository.findByPetOwnerIdAndPetActiveTrueAndSentFalseOrderByDueDateAsc(user.getId());
        return alerts.stream().map(alertMapper::toResponseDTO).toList();
    }

    @Transactional
    @PreAuthorize("hasRole('CLINICA')")
    public AlertResponseDTO create(AlertRequestDTO dto) {
        Pet pet = petAccessPolicy.requireActivePet(dto.getPetId());
        Alert alert = alertMapper.toEntity(dto, pet);
        return alertMapper.toResponseDTO(alertRepository.save(alert));
    }

    @Transactional
    @PreAuthorize("hasRole('CLINICA')")
    public AlertResponseDTO update(Long id, AlertRequestDTO dto) {
        Alert alert = findAlertById(id);
        petAccessPolicy.requireActivePet(alert.getPet().getId());
        alertMapper.updateEntity(alert, dto);
        return alertMapper.toResponseDTO(alertRepository.save(alert));
    }

    @Transactional
    @PreAuthorize("hasRole('CLINICA')")
    public AlertResponseDTO markAsSent(Long id) {
        Alert alert = findAlertById(id);
        petAccessPolicy.requireActivePet(alert.getPet().getId());
        alert.setSent(true);
        return alertMapper.toResponseDTO(alertRepository.save(alert));
    }

    @Transactional
    @PreAuthorize("hasRole('CLINICA')")
    public void delete(Long id) {
        Alert alert = findAlertById(id);
        petAccessPolicy.requireActivePet(alert.getPet().getId());
        alertRepository.delete(alert);
    }

    private Alert findAlertById(Long id) {
        return alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta", id));
    }
}
