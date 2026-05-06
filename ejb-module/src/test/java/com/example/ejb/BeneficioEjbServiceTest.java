package com.example.ejb;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import com.example.ejb.entities.Beneficio;
import com.example.ejb.services.BeneficioEjbService;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BeneficioEjbServiceTest {

    private EntityManager em;
    private BeneficioEjbService service;

    private Beneficio beneficioA;
    private Beneficio beneficioB;

    @BeforeEach
    void setUp() {
        em = mock(EntityManager.class);
        service = new BeneficioEjbService(em);
        beneficioA = new Beneficio(1L, new BigDecimal("1000.00"));
        beneficioB = new Beneficio(2L, new BigDecimal("500.00"));
    }

    @Test
    void transfer_success_updatesBalancesWithPessimisticLocks() {
        when(em.find(Beneficio.class, 1L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(beneficioA);
        when(em.find(Beneficio.class, 2L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(beneficioB);

        service.transfer(1L, 2L, new BigDecimal("200.00"));

        assertThat(beneficioA.getValor()).isEqualByComparingTo("800.00");
        assertThat(beneficioB.getValor()).isEqualByComparingTo("700.00");
        verify(em).merge(beneficioA);
        verify(em).merge(beneficioB);
    }

    @Test
    void transfer_locksInConsistentIdOrder() {
        when(em.find(Beneficio.class, 1L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(beneficioA);
        when(em.find(Beneficio.class, 2L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(beneficioB);

        service.transfer(2L, 1L, new BigDecimal("100.00"));

        InOrder inOrder = inOrder(em);
        inOrder.verify(em).find(Beneficio.class, 1L, LockModeType.PESSIMISTIC_WRITE);
        inOrder.verify(em).find(Beneficio.class, 2L, LockModeType.PESSIMISTIC_WRITE);
        assertThat(beneficioA.getValor()).isEqualByComparingTo("1100.00");
        assertThat(beneficioB.getValor()).isEqualByComparingTo("400.00");
    }

    @Test
    void transfer_failsWithoutChangingStateWhenBalanceIsInsufficient() {
        when(em.find(Beneficio.class, 1L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(beneficioA);
        when(em.find(Beneficio.class, 2L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(beneficioB);

        assertThatThrownBy(() -> service.transfer(1L, 2L, new BigDecimal("2000.00")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Saldo insuficiente");

        assertThat(beneficioA.getValor()).isEqualByComparingTo("1000.00");
        assertThat(beneficioB.getValor()).isEqualByComparingTo("500.00");
        verify(em, never()).merge(beneficioA);
        verify(em, never()).merge(beneficioB);
    }

    @Test
    void transfer_rejectsInvalidInputsBeforeLocking() {
        assertThatThrownBy(() -> service.transfer(1L, 1L, new BigDecimal("10.00")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não podem ser iguais");
        assertThatThrownBy(() -> service.transfer(1L, 2L, BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maior que zero");
        assertThatThrownBy(() -> service.transfer(null, 2L, new BigDecimal("10.00")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("origem");

        verify(em, never()).find(Beneficio.class, 1L, LockModeType.PESSIMISTIC_WRITE);
    }

    @Test
    void transfer_throwsNotFoundWhenAnyBeneficioDoesNotExist() {
        when(em.find(Beneficio.class, 1L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(beneficioA);
        when(em.find(Beneficio.class, 2L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(null);

        assertThatThrownBy(() -> service.transfer(1L, 2L, new BigDecimal("10.00")))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("2");

        verify(em, never()).merge(beneficioA);
    }
}
