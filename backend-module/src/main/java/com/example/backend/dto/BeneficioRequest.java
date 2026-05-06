package com.example.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class BeneficioRequest {

    @NotBlank(message = "Nome é obrigatório.")
    private String nome;

    private String descricao;

    @NotNull(message = "Valor é obrigatório.")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero.")
    private BigDecimal valor;

    private Boolean ativo = Boolean.TRUE;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
}
