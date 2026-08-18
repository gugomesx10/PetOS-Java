package br.com.petos.project.service;

import br.com.petos.project.dto.RoutineRequestDTO;
import br.com.petos.project.dto.RoutineResponseDTO;
import br.com.petos.project.entity.Pet;
import br.com.petos.project.entity.RoutineRecord;
import br.com.petos.project.exception.ResourceNotFoundException;
import br.com.petos.project.mapper.RoutineRecordMapper;
import br.com.petos.project.repository.RoutineRecordRepository;
import br.com.petos.project.security.AuthenticatedUser;
import br.com.petos.project.security.CurrentUserProvider;
import br.com.petos.project.security.PetAccessPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoutineRecordService {

    private final RoutineRecordRepository routineRecordRepository;
    private final RoutineRecordMapper routineRecordMapper;
    private final PetAccessPolicy petAccessPolicy;
    private final CurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    public Page<RoutineResponseDTO> findAll(Pageable pageable) {
        AuthenticatedUser user = currentUserProvider.require();
        Page<RoutineRecord> records = user.isClinica()
                ? routineRecordRepository.findByPetActiveTrue(pageable)
                : routineRecordRepository.findByPetOwnerIdAndPetActiveTrue(user.getId(), pageable);
        return records.map(routineRecordMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public RoutineResponseDTO findById(Long id) {
        RoutineRecord record = findRecordById(id);
        petAccessPolicy.requireReadable(record.getPet().getId());
        return routineRecordMapper.toResponseDTO(record);
    }

    @Transactional(readOnly = true)
    public List<RoutineResponseDTO> findByPetId(Long petId) {
        petAccessPolicy.requireReadable(petId);
        return routineRecordRepository.findByPetIdOrderByRecordDateDesc(petId)
                .stream().map(routineRecordMapper::toResponseDTO).toList();
    }

    @Transactional
    public RoutineResponseDTO create(RoutineRequestDTO dto) {
        Pet pet = petAccessPolicy.requireReadable(dto.getPetId());
        RoutineRecord record = routineRecordMapper.toEntity(dto, pet);
        return routineRecordMapper.toResponseDTO(routineRecordRepository.save(record));
    }

    @Transactional
    public RoutineResponseDTO update(Long id, RoutineRequestDTO dto) {
        RoutineRecord record = findRecordById(id);
        petAccessPolicy.requireReadable(record.getPet().getId());
        routineRecordMapper.updateEntity(record, dto);
        return routineRecordMapper.toResponseDTO(routineRecordRepository.save(record));
    }

    @Transactional
    public void delete(Long id) {
        RoutineRecord record = findRecordById(id);
        petAccessPolicy.requireReadable(record.getPet().getId());
        routineRecordRepository.delete(record);
    }

    private RoutineRecord findRecordById(Long id) {
        return routineRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registro de rotina", id));
    }
}
