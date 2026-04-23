package br.com.petos.project.controller;

import br.com.petos.project.dto.RoutineRequestDTO;
import br.com.petos.project.dto.RoutineResponseDTO;
import br.com.petos.project.service.RoutineRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Routines", description = "Gerenciamento de rotinas e cuidados do pet")
public class RoutineRecordController {

    private final RoutineRecordService routineRecordService;

    @GetMapping("/routines")
    @Operation(summary = "Listar todos os registros de rotina com paginação")
    public ResponseEntity<Page<RoutineResponseDTO>> findAll(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(routineRecordService.findAll(pageable));
    }

    @GetMapping("/routines/{id}")
    @Operation(summary = "Buscar registro de rotina por ID")
    public ResponseEntity<RoutineResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(routineRecordService.findById(id));
    }

    @GetMapping("/pets/{petId}/routines")
    @Operation(summary = "Listar rotinas de um pet em ordem cronológica decrescente")
    public ResponseEntity<List<RoutineResponseDTO>> findByPetId(@PathVariable Long petId) {
        return ResponseEntity.ok(routineRecordService.findByPetId(petId));
    }

    @PostMapping("/routines")
    @Operation(summary = "Registrar nova rotina/cuidado")
    public ResponseEntity<RoutineResponseDTO> create(@Valid @RequestBody RoutineRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(routineRecordService.create(dto));
    }

    @PutMapping("/routines/{id}")
    @Operation(summary = "Atualizar registro de rotina")
    public ResponseEntity<RoutineResponseDTO> update(@PathVariable Long id, @Valid @RequestBody RoutineRequestDTO dto) {
        return ResponseEntity.ok(routineRecordService.update(id, dto));
    }

    @DeleteMapping("/routines/{id}")
    @Operation(summary = "Remover registro de rotina")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        routineRecordService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

