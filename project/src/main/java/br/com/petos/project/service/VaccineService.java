package br.com.petos.project.service;

import br.com.petos.project.domain.VaccinationPolicy;
import br.com.petos.project.dto.VaccineRequestDTO;
import br.com.petos.project.dto.VaccineResponseDTO;
import br.com.petos.project.entity.Pet;
import br.com.petos.project.entity.Vaccine;
import br.com.petos.project.exception.BusinessRuleException;
import br.com.petos.project.exception.ResourceNotFoundException;
import br.com.petos.project.mapper.VaccineMapper;
import br.com.petos.project.repository.VaccineRepository;
import br.com.petos.project.security.AuthenticatedUser;
import br.com.petos.project.security.CurrentUserProvider;
import br.com.petos.project.security.PetAccessPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VaccineService {

    private final VaccineRepository vaccineRepository;
    private final VaccineMapper vaccineMapper;
    private final VaccineAlertService vaccineAlertService;
    private final PetAccessPolicy petAccessPolicy;
    private final CurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    public Page<VaccineResponseDTO> findAll(Pageable pageable) {
        AuthenticatedUser user = currentUserProvider.require();
        Page<Vaccine> vaccines = user.isClinica()
                ? vaccineRepository.findByPetActiveTrue(pageable)
                : vaccineRepository.findByPetOwnerIdAndPetActiveTrue(user.getId(), pageable);
        return vaccines.map(vaccineMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public VaccineResponseDTO findById(Long id) {
        return vaccineMapper.toResponseDTO(findReadableVaccine(id));
    }

    @Transactional(readOnly = true)
    public List<VaccineResponseDTO> findByPetId(Long petId) {
        petAccessPolicy.requireReadable(petId);
        return vaccineRepository.findByPetIdOrderByDueDateAsc(petId)
                .stream().map(vaccineMapper::toResponseDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<VaccineResponseDTO> findPendingByPetId(Long petId) {
        petAccessPolicy.requireReadable(petId);
        return vaccineRepository
                .findByPetIdAndStatusInOrderByDueDateAsc(petId, VaccinationPolicy.PENDING_STATUSES)
                .stream().map(vaccineMapper::toResponseDTO).toList();
    }

    @Transactional
    @PreAuthorize("hasRole('CLINICA')")
    public VaccineResponseDTO create(VaccineRequestDTO dto) {
        Pet pet = petAccessPolicy.requireActivePet(dto.getPetId());
        validateDates(dto);

        Vaccine vaccine = vaccineMapper.toEntity(dto, pet);
        vaccine.refreshStatus(LocalDate.now());
        vaccine = vaccineRepository.save(vaccine);

        vaccineAlertService.synchronizePreventiveAlert(vaccine);
        return vaccineMapper.toResponseDTO(vaccine);
    }

    @Transactional
    @PreAuthorize("hasRole('CLINICA')")
    public VaccineResponseDTO update(Long id, VaccineRequestDTO dto) {
        Vaccine vaccine = findVaccineById(id);
        petAccessPolicy.requireActivePet(vaccine.getPet().getId());
        validateDates(dto);

        vaccineMapper.updateEntity(vaccine, dto);
        vaccine.refreshStatus(LocalDate.now());
        vaccine = vaccineRepository.save(vaccine);

        vaccineAlertService.synchronizePreventiveAlert(vaccine);
        return vaccineMapper.toResponseDTO(vaccine);
    }

    @Transactional
    @PreAuthorize("hasRole('CLINICA')")
    public void delete(Long id) {
        Vaccine vaccine = findVaccineById(id);
        petAccessPolicy.requireActivePet(vaccine.getPet().getId());
        vaccineRepository.delete(vaccine);
    }

    private void validateDates(VaccineRequestDTO dto) {
        if (dto.getApplicationDate() != null && dto.getDueDate() != null
                && dto.getDueDate().isBefore(dto.getApplicationDate())) {
            throw new BusinessRuleException(
                    "Data de vencimento não pode ser anterior à data de aplicação.");
        }
    }

    private Vaccine findReadableVaccine(Long id) {
        Vaccine vaccine = findVaccineById(id);
        petAccessPolicy.requireReadable(vaccine.getPet().getId());
        return vaccine;
    }

    private Vaccine findVaccineById(Long id) {
        return vaccineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vacina", id));
    }
}
