package com.example.backend.dto;

import com.example.backend.models.BeneficioEntity;
import java.math.BigDecimal;

public class BeneficioResponse {

    private Long id;
    private String nome;
    private String descricao;
    private BigDecimal valor;
    private Boolean ativo;

    public static BeneficioResponse from(BeneficioEntity entity) {
        BeneficioResponse dto = new BeneficioResponse();
        dto.id = entity.getId();
        dto.nome = entity.getNome();
        dto.descricao = entity.getDescricao();
        dto.valor = entity.getValor();
        dto.ativo = entity.getAtivo();
        return dto;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public BigDecimal getValor() { return valor; }
    public Boolean getAtivo() { return ativo; }
}
