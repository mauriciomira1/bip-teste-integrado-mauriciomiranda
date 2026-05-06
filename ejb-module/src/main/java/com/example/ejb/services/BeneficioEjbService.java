package com.example.ejb.services;

import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;

import com.example.ejb.entities.Beneficio;

@Stateless
public class BeneficioEjbService {

    @PersistenceContext
    private EntityManager em;

    public BeneficioEjbService() {
    }

    public BeneficioEjbService(EntityManager em) {
        this.em = em;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void transfer(Long fromId, Long toId, BigDecimal amount) {
        if (fromId == null) {
            throw new IllegalArgumentException("ID de origem é obrigatório");
        }
        if (toId == null) {
            throw new IllegalArgumentException("ID de destino é obrigatório");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor de transferência deve ser maior que zero");
        }
        if (fromId.equals(toId)) {
            throw new IllegalArgumentException("Origem e destino não podem ser iguais");
        }

        // Adquire locks em ordem consistente para evitar deadlock
        Long firstId = Math.min(fromId, toId);
        Long secondId = Math.max(fromId, toId);

        Beneficio first = em.find(Beneficio.class, firstId, LockModeType.PESSIMISTIC_WRITE);
        Beneficio second = em.find(Beneficio.class, secondId, LockModeType.PESSIMISTIC_WRITE);

        if (first == null || second == null) {
            Long missingId = first == null ? firstId : secondId;
            throw new EntityNotFoundException("Benefício não encontrado: " + missingId);
        }

        Beneficio from = fromId.equals(firstId) ? first : second;
        Beneficio to = fromId.equals(firstId) ? second : first;

        if (from.getValor().compareTo(amount) < 0) {
            throw new IllegalArgumentException(
                    "Saldo insuficiente. Disponível: " + from.getValor() + ", solicitado: " + amount);
        }

        from.setValor(from.getValor().subtract(amount));
        to.setValor(to.getValor().add(amount));

        // EntityManager rastreia entidades gerenciadas; merge explícito garante
        // persistência
        em.merge(from);
        em.merge(to);
    }
}

/* Entidade realocada para /entities/Beneficio.java */