package br.com.petos.project.service;

import br.com.petos.project.domain.VaccinationPolicy;
import br.com.petos.project.dto.PetRequestDTO;
import br.com.petos.project.dto.PetResponseDTO;
import br.com.petos.project.entity.Pet;
import br.com.petos.project.entity.User;
import br.com.petos.project.enums.Species;
import br.com.petos.project.enums.VaccineStatus;
import br.com.petos.project.mapper.PetMapper;
import br.com.petos.project.repository.PetRepository;
import br.com.petos.project.repository.UserRepository;
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
public class PetService {

    private final PetRepository petRepository;
    private final UserRepository userRepository;
    private final PetMapper petMapper;
    private final PetAccessPolicy petAccessPolicy;
    private final CurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    public Page<PetResponseDTO> findAll(Pageable pageable) {
        AuthenticatedUser user = currentUserProvider.require();
        Page<Pet> pets = user.isClinica()
                ? petRepository.findByActiveTrue(pageable)
                : petRepository.findByOwnerIdAndActiveTrue(user.getId(), pageable);
        return pets.map(petMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public PetResponseDTO findById(Long id) {
        return petMapper.toResponseDTO(petAccessPolicy.requireReadable(id));
    }

    @Transactional(readOnly = true)
    public Page<PetResponseDTO> searchByName(String name, Pageable pageable) {
        AuthenticatedUser user = currentUserProvider.require();
        Page<Pet> pets = user.isClinica()
                ? petRepository.findByNameContainingIgnoreCaseAndActiveTrue(name, pageable)
                : petRepository.findByOwnerIdAndNameContainingIgnoreCaseAndActiveTrue(user.getId(), name, pageable);
        return pets.map(petMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<PetResponseDTO> findBySpecies(Species species, Pageable pageable) {
        AuthenticatedUser user = currentUserProvider.require();
        Page<Pet> pets = user.isClinica()
                ? petRepository.findBySpeciesAndActiveTrue(species, pageable)
                : petRepository.findByOwnerIdAndSpeciesAndActiveTrue(user.getId(), species, pageable);
        return pets.map(petMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public List<PetResponseDTO> findPetsWithVaccinesDueOrExpiringSoon() {
        AuthenticatedUser user = currentUserProvider.require();
        LocalDate threshold = VaccinationPolicy.warningThreshold(LocalDate.now());
        List<Pet> pets = user.isClinica()
                ? petRepository.findActivePetsWithVaccinesDueUntil(threshold, VaccineStatus.APPLIED)
                : petRepository.findActivePetsByOwnerWithVaccinesDueUntil(user.getId(), threshold, VaccineStatus.APPLIED);
        return pets.stream().map(petMapper::toResponseDTO).toList();
    }

    @Transactional
    @PreAuthorize("hasRole('TUTOR')")
    public PetResponseDTO create(PetRequestDTO dto) {
        User owner = userRepository.getReferenceById(currentUserProvider.require().getId());
        Pet pet = petMapper.toEntity(dto);
        pet.setOwner(owner);
        return petMapper.toResponseDTO(petRepository.save(pet));
    }

    @Transactional
    @PreAuthorize("hasRole('TUTOR')")
    public PetResponseDTO update(Long id, PetRequestDTO dto) {
        Pet pet = petAccessPolicy.requireOwned(id);
        petMapper.updateEntity(pet, dto);
        return petMapper.toResponseDTO(petRepository.save(pet));
    }

    /**
     * Soft delete: o pet e inativado e deixa de ser acessivel pela API,
     * mas o historico clinico permanece integro no banco.
     */
    @Transactional
    @PreAuthorize("hasRole('TUTOR')")
    public void delete(Long id) {
        Pet pet = petAccessPolicy.requireOwned(id);
        pet.setActive(false);
        petRepository.save(pet);
    }
}
