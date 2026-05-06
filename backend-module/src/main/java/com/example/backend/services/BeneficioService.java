package com.example.backend.services;

import com.example.backend.dto.BeneficioRequest;
import com.example.backend.dto.BeneficioResponse;
import com.example.backend.dto.TransferRequest;
import com.example.backend.models.BeneficioEntity;
import com.example.backend.repositories.BeneficioRepository;
import com.example.ejb.services.BeneficioEjbService;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BeneficioService {

    private final BeneficioRepository repository;
    private final BeneficioEjbService beneficioEjbService;

    public BeneficioService(BeneficioRepository repository, BeneficioEjbService beneficioEjbService) {
        this.repository = repository;
        this.beneficioEjbService = beneficioEjbService;
    }

    public List<BeneficioResponse> findAll() {
        return repository.findByAtivoTrue()
                .stream()
                .map(BeneficioResponse::from)
                .collect(Collectors.toList());
    }

    public BeneficioResponse findById(Long id) {
        return BeneficioResponse.from(getOrThrow(id));
    }

    @Transactional
    public BeneficioResponse create(BeneficioRequest request) {
        BeneficioEntity entity = new BeneficioEntity(
                request.getNome(),
                request.getDescricao(),
                request.getValor());
        if (request.getAtivo() != null) {
            entity.setAtivo(request.getAtivo());
        }
        return BeneficioResponse.from(repository.save(entity));
    }

    @Transactional
    public BeneficioResponse update(Long id, BeneficioRequest request) {
        BeneficioEntity entity = getOrThrow(id);
        entity.setNome(request.getNome());
        entity.setDescricao(request.getDescricao());
        entity.setValor(request.getValor());
        if (request.getAtivo() != null) {
            entity.setAtivo(request.getAtivo());
        }
        return BeneficioResponse.from(repository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        BeneficioEntity entity = getOrThrow(id);
        entity.setAtivo(Boolean.FALSE);
        repository.save(entity);
    }

    @Transactional
    public void transfer(TransferRequest request) {
        beneficioEjbService.transfer(request.getFromId(), request.getToId(), request.getAmount());
    }

    private BeneficioEntity getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Benefício não encontrado: " + id));
    }
}
