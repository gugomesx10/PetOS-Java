package br.com.petos.project.controller;

import br.com.petos.project.dto.VaccineRequestDTO;
import br.com.petos.project.dto.VaccineResponseDTO;
import br.com.petos.project.service.VaccineService;
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
@Tag(name = "Vaccines", description = "Gerenciamento de vacinas")
public class VaccineController {

    private final VaccineService vaccineService;

    @GetMapping("/vaccines")
    @Operation(summary = "Listar todas as vacinas com paginação")
    public ResponseEntity<Page<VaccineResponseDTO>> findAll(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(vaccineService.findAll(pageable));
    }

    @GetMapping("/vaccines/{id}")
    @Operation(summary = "Buscar vacina por ID")
    public ResponseEntity<VaccineResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(vaccineService.findById(id));
    }

    @GetMapping("/pets/{petId}/vaccines")
    @Operation(summary = "Listar vacinas de um pet")
    public ResponseEntity<List<VaccineResponseDTO>> findByPetId(@PathVariable Long petId) {
        return ResponseEntity.ok(vaccineService.findByPetId(petId));
    }

    @GetMapping("/pets/{petId}/vaccines/pending")
    @Operation(summary = "Listar vacinas pendentes de um pet")
    public ResponseEntity<List<VaccineResponseDTO>> findPendingByPetId(@PathVariable Long petId) {
        return ResponseEntity.ok(vaccineService.findPendingByPetId(petId));
    }

    @PostMapping("/vaccines")
    @Operation(summary = "Registrar nova vacina (gera alerta automático se houver vencimento futuro)")
    public ResponseEntity<VaccineResponseDTO> create(@Valid @RequestBody VaccineRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vaccineService.create(dto));
    }

    @PutMapping("/vaccines/{id}")
    @Operation(summary = "Atualizar vacina")
    public ResponseEntity<VaccineResponseDTO> update(@PathVariable Long id, @Valid @RequestBody VaccineRequestDTO dto) {
        return ResponseEntity.ok(vaccineService.update(id, dto));
    }

    @DeleteMapping("/vaccines/{id}")
    @Operation(summary = "Remover vacina")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        vaccineService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

