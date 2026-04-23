package br.com.petos.project.controller;

import br.com.petos.project.dto.AlertRequestDTO;
import br.com.petos.project.dto.AlertResponseDTO;
import br.com.petos.project.service.AlertService;
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
@Tag(name = "Alerts", description = "Gerenciamento de alertas do pet")
public class AlertController {

    private final AlertService alertService;

    @GetMapping("/alerts")
    @Operation(summary = "Listar todos os alertas com paginação")
    public ResponseEntity<Page<AlertResponseDTO>> findAll(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(alertService.findAll(pageable));
    }

    @GetMapping("/alerts/{id}")
    @Operation(summary = "Buscar alerta por ID")
    public ResponseEntity<AlertResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(alertService.findById(id));
    }

    @GetMapping("/alerts/pending")
    @Operation(summary = "Listar todos os alertas pendentes (não enviados)")
    public ResponseEntity<List<AlertResponseDTO>> findAllPending() {
        return ResponseEntity.ok(alertService.findAllPending());
    }

    @GetMapping("/pets/{petId}/alerts")
    @Operation(summary = "Listar todos os alertas de um pet")
    public ResponseEntity<List<AlertResponseDTO>> findByPetId(@PathVariable Long petId) {
        return ResponseEntity.ok(alertService.findByPetId(petId));
    }

    @GetMapping("/pets/{petId}/alerts/pending")
    @Operation(summary = "Listar alertas pendentes de um pet")
    public ResponseEntity<List<AlertResponseDTO>> findPendingByPetId(@PathVariable Long petId) {
        return ResponseEntity.ok(alertService.findPendingByPetId(petId));
    }

    @PostMapping("/alerts")
    @Operation(summary = "Criar novo alerta manualmente")
    public ResponseEntity<AlertResponseDTO> create(@Valid @RequestBody AlertRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(alertService.create(dto));
    }

    @PutMapping("/alerts/{id}")
    @Operation(summary = "Atualizar alerta")
    public ResponseEntity<AlertResponseDTO> update(@PathVariable Long id, @Valid @RequestBody AlertRequestDTO dto) {
        return ResponseEntity.ok(alertService.update(id, dto));
    }

    @PatchMapping("/alerts/{id}/mark-sent")
    @Operation(summary = "Marcar alerta como enviado")
    public ResponseEntity<AlertResponseDTO> markAsSent(@PathVariable Long id) {
        return ResponseEntity.ok(alertService.markAsSent(id));
    }

    @DeleteMapping("/alerts/{id}")
    @Operation(summary = "Remover alerta")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        alertService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

