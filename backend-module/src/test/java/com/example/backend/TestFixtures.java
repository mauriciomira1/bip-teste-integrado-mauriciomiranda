package com.example.backend;

import com.example.backend.dto.BeneficioRequest;
import com.example.backend.dto.TransferRequest;
import com.example.backend.models.BeneficioEntity;

import java.math.BigDecimal;

public final class TestFixtures {

    private TestFixtures() {}

    public static BeneficioEntity beneficio(Long id, String nome, BigDecimal valor) {
        BeneficioEntity entity = new BeneficioEntity(nome, "Descrição " + nome, valor);
        entity.setAtivo(true);
        entity.setId(id);
        return entity;
    }

    public static BeneficioEntity beneficio(String nome, BigDecimal valor) {
        BeneficioEntity entity = new BeneficioEntity(nome, "Descrição " + nome, valor);
        entity.setAtivo(true);
        return entity;
    }

    public static BeneficioRequest request(String nome, BigDecimal valor) {
        BeneficioRequest req = new BeneficioRequest();
        req.setNome(nome);
        req.setValor(valor);
        req.setDescricao("Descrição " + nome);
        return req;
    }

    public static TransferRequest transferRequest(Long fromId, Long toId, BigDecimal amount) {
        TransferRequest req = new TransferRequest();
        req.setFromId(fromId);
        req.setToId(toId);
        req.setAmount(amount);
        return req;
    }
}
