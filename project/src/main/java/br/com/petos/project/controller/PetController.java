package br.com.petos.project.controller;

import br.com.petos.project.dto.PetHistoryDTO;
import br.com.petos.project.dto.PetRequestDTO;
import br.com.petos.project.dto.PetResponseDTO;
import br.com.petos.project.enums.Species;
import br.com.petos.project.service.PetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pets")
@RequiredArgsConstructor
@Tag(name = "Pets", description = "Gerenciamento de pets")
public class PetController {

    private final PetService petService;

    @GetMapping
    @Operation(summary = "Listar todos os pets ativos com paginação")
    public ResponseEntity<Page<PetResponseDTO>> findAll(
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(petService.findAll(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar pet por ID")
    public ResponseEntity<PetResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(petService.findById(id));
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar pets por nome")
    public ResponseEntity<Page<PetResponseDTO>> searchByName(
            @RequestParam String name,
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(petService.searchByName(name, pageable));
    }

    @GetMapping("/species/{species}")
    @Operation(summary = "Filtrar pets por espécie")
    public ResponseEntity<Page<PetResponseDTO>> findBySpecies(
            @PathVariable Species species,
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(petService.findBySpecies(species, pageable));
    }

    @GetMapping("/vaccines/expiring")
    @Operation(summary = "Listar pets com vacinas vencidas ou próximas do vencimento")
    public ResponseEntity<List<PetResponseDTO>> findPetsWithVaccinesDueOrExpiringSoon() {
        return ResponseEntity.ok(petService.findPetsWithVaccinesDueOrExpiringSoon());
    }

    @GetMapping("/{id}/history")
    @Operation(summary = "Histórico consolidado do pet: vacinas, rotinas e alertas")
    public ResponseEntity<PetHistoryDTO> getPetHistory(@PathVariable Long id) {
        return ResponseEntity.ok(petService.getPetHistory(id));
    }

    @PostMapping
    @Operation(summary = "Cadastrar novo pet")
    public ResponseEntity<PetResponseDTO> create(@Valid @RequestBody PetRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(petService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar dados do pet")
    public ResponseEntity<PetResponseDTO> update(@PathVariable Long id, @Valid @RequestBody PetRequestDTO dto) {
        return ResponseEntity.ok(petService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Inativar pet (soft delete)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        petService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

