package br.com.petos.project.service;

import br.com.petos.project.dto.PetHistoryDTO;
import br.com.petos.project.dto.PetRequestDTO;
import br.com.petos.project.dto.PetResponseDTO;
import br.com.petos.project.entity.Pet;
import br.com.petos.project.enums.Species;
import br.com.petos.project.exception.ResourceNotFoundException;
import br.com.petos.project.mapper.AlertMapper;
import br.com.petos.project.mapper.PetMapper;
import br.com.petos.project.mapper.RoutineRecordMapper;
import br.com.petos.project.mapper.VaccineMapper;
import br.com.petos.project.repository.PetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PetService {

    private final PetRepository petRepository;
    private final PetMapper petMapper;
    private final VaccineMapper vaccineMapper;
    private final RoutineRecordMapper routineRecordMapper;
    private final AlertMapper alertMapper;

    @Cacheable("pets")
    @Transactional(readOnly = true)
    public Page<PetResponseDTO> findAll(Pageable pageable) {
        return petRepository.findByActiveTrue(pageable)
                .map(petMapper::toResponseDTO);
    }

    @Cacheable(value = "pet", key = "#id")
    @Transactional(readOnly = true)
    public PetResponseDTO findById(Long id) {
        return petMapper.toResponseDTO(findPetById(id));
    }

    @Transactional(readOnly = true)
    public Page<PetResponseDTO> searchByName(String name, Pageable pageable) {
        return petRepository.findByNameContainingIgnoreCaseAndActiveTrue(name, pageable)
                .map(petMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<PetResponseDTO> findBySpecies(Species species, Pageable pageable) {
        return petRepository.findBySpeciesAndActiveTrue(species, pageable)
                .map(petMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public List<PetResponseDTO> findPetsWithVaccinesDueOrExpiringSoon() {
        return petRepository.findPetsWithVaccinesDueOrExpiringSoon()
                .stream()
                .map(petMapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public PetHistoryDTO getPetHistory(Long id) {
        Pet pet = findPetById(id);
        return PetHistoryDTO.builder()
                .pet(petMapper.toResponseDTO(pet))
                .vaccines(pet.getVaccines().stream().map(vaccineMapper::toResponseDTO).toList())
                .routines(pet.getRoutineRecords().stream().map(routineRecordMapper::toResponseDTO).toList())
                .alerts(pet.getAlerts().stream().map(alertMapper::toResponseDTO).toList())
                .build();
    }

    @CacheEvict(value = {"pets", "pet"}, allEntries = true)
    @Transactional
    public PetResponseDTO create(PetRequestDTO dto) {
        Pet pet = petMapper.toEntity(dto);
        return petMapper.toResponseDTO(petRepository.save(pet));
    }

    @CacheEvict(value = {"pets", "pet"}, allEntries = true)
    @Transactional
    public PetResponseDTO update(Long id, PetRequestDTO dto) {
        Pet pet = findPetById(id);
        petMapper.updateEntity(pet, dto);
        return petMapper.toResponseDTO(petRepository.save(pet));
    }

    @CacheEvict(value = {"pets", "pet"}, allEntries = true)
    @Transactional
    public void delete(Long id) {
        Pet pet = findPetById(id);
        pet.setActive(false);
        petRepository.save(pet);
    }

    public Pet findPetById(Long id) {
        return petRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pet", id));
    }
}

