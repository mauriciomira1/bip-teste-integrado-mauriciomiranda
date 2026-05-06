package com.example.backend.controllers;

import com.example.backend.dto.BeneficioRequest;
import com.example.backend.dto.BeneficioResponse;
import com.example.backend.dto.TransferRequest;
import com.example.backend.services.BeneficioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/beneficios")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Benefícios", description = "CRUD e transferência de benefícios")
public class BeneficioController {

    private final BeneficioService service;

    public BeneficioController(BeneficioService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista todos os benefícios ativos")
    public ResponseEntity<List<BeneficioResponse>> list() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(service.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca benefício por ID")
    public ResponseEntity<BeneficioResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    @Operation(summary = "Cria um novo benefício")
    public ResponseEntity<BeneficioResponse> create(@Valid @RequestBody BeneficioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um benefício existente")
    public ResponseEntity<BeneficioResponse> update(@PathVariable Long id,
                                                     @Valid @RequestBody BeneficioRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Inativa um benefício (soft delete)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/transfer")
    @Operation(summary = "Transfere valor entre dois benefícios com lock e validação de saldo")
    public ResponseEntity<Void> transfer(@Valid @RequestBody TransferRequest request) {
        service.transfer(request);
        return ResponseEntity.noContent().build();
    }
}
