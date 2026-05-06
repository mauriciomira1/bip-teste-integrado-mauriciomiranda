package com.example.backend.services;

import com.example.backend.TestFixtures;
import com.example.backend.dto.BeneficioRequest;
import com.example.backend.dto.BeneficioResponse;
import com.example.backend.dto.TransferRequest;
import com.example.backend.models.BeneficioEntity;
import com.example.backend.repositories.BeneficioRepository;
import com.example.ejb.services.BeneficioEjbService;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BeneficioServiceTest {

    @Mock
    private BeneficioRepository repository;

    @Mock
    private BeneficioEjbService beneficioEjbService;

    @InjectMocks
    private BeneficioService service;

    private BeneficioEntity entityA;
    private BeneficioEntity entityB;

    @BeforeEach
    void setUp() {
        entityA = TestFixtures.beneficio(1L, "A", new BigDecimal("1000.00"));
        entityB = TestFixtures.beneficio(2L, "B", new BigDecimal("500.00"));
    }

    @Test
    @DisplayName("findAll retorna apenas benefícios ativos mapeados em response")
    void findAll_returnsOnlyActive() {
        when(repository.findByAtivoTrue()).thenReturn(List.of(entityA, entityB));

        List<BeneficioResponse> result = service.findAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(BeneficioResponse::getNome).containsExactly("A", "B");
        verify(repository).findByAtivoTrue();
    }

    @Test
    void findById_returnsResponseWhenFound() {
        when(repository.findById(1L)).thenReturn(Optional.of(entityA));

        BeneficioResponse response = service.findById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getNome()).isEqualTo("A");
        assertThat(response.getValor()).isEqualByComparingTo("1000.00");
    }

    @Test
    void findById_throwsWhenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_persistsEntityAndReturnsResponse() {
        BeneficioRequest req = TestFixtures.request("Novo", new BigDecimal("200.00"));
        when(repository.save(any(BeneficioEntity.class))).thenAnswer(inv -> {
            BeneficioEntity e = inv.getArgument(0);
            e.setId(10L);
            return e;
        });

        BeneficioResponse response = service.create(req);

        ArgumentCaptor<BeneficioEntity> captor = ArgumentCaptor.forClass(BeneficioEntity.class);
        verify(repository).save(captor.capture());
        BeneficioEntity saved = captor.getValue();
        assertThat(saved.getNome()).isEqualTo("Novo");
        assertThat(saved.getValor()).isEqualByComparingTo("200.00");
        assertThat(saved.getAtivo()).isTrue();
        assertThat(response.getId()).isEqualTo(10L);
    }

    @Test
    void create_respectsAtivoFalseFromRequest() {
        BeneficioRequest req = TestFixtures.request("Inativo", new BigDecimal("50.00"));
        req.setAtivo(false);
        when(repository.save(any(BeneficioEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        service.create(req);

        ArgumentCaptor<BeneficioEntity> captor = ArgumentCaptor.forClass(BeneficioEntity.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getAtivo()).isFalse();
    }

    @Test
    void update_persistsChangesAndReturnsResponse() {
        when(repository.findById(1L)).thenReturn(Optional.of(entityA));
        when(repository.save(any(BeneficioEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        BeneficioRequest req = TestFixtures.request("A atualizado", new BigDecimal("1500.00"));
        BeneficioResponse response = service.update(1L, req);

        assertThat(response.getNome()).isEqualTo("A atualizado");
        assertThat(response.getValor()).isEqualByComparingTo("1500.00");
        assertThat(entityA.getNome()).isEqualTo("A atualizado");
        verify(repository).save(entityA);
    }

    @Test
    void update_throwsWhenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        BeneficioRequest req = TestFixtures.request("X", new BigDecimal("1.00"));
        assertThatThrownBy(() -> service.update(99L, req))
                .isInstanceOf(EntityNotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void delete_setsAtivoFalse() {
        when(repository.findById(1L)).thenReturn(Optional.of(entityA));
        when(repository.save(any())).thenReturn(entityA);

        service.delete(1L);

        assertThat(entityA.getAtivo()).isFalse();
        verify(repository).save(entityA);
    }

    @Test
    void delete_throwsWhenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(EntityNotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void transfer_success_delegatesToEjbService() {
        TransferRequest req = TestFixtures.transferRequest(1L, 2L, new BigDecimal("200.00"));

        service.transfer(req);

        verify(beneficioEjbService).transfer(1L, 2L, new BigDecimal("200.00"));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("transfer com fromId > toId mantém IDs originais para o EJB aplicar ordenação anti-deadlock")
    void transfer_withReversedIds_delegatesOriginalRequestToEjb() {
        TransferRequest req = TestFixtures.transferRequest(2L, 1L, new BigDecimal("100.00"));

        service.transfer(req);

        verify(beneficioEjbService).transfer(2L, 1L, new BigDecimal("100.00"));
    }

    @Test
    void transfer_propagatesInsufficientBalanceFromEjb() {
        TransferRequest req = TestFixtures.transferRequest(1L, 2L, new BigDecimal("9999.00"));
        doThrow(new IllegalArgumentException("Saldo insuficiente"))
                .when(beneficioEjbService).transfer(1L, 2L, new BigDecimal("9999.00"));

        assertThatThrownBy(() -> service.transfer(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Saldo insuficiente");

        verify(repository, never()).save(any());
    }

    @Test
    void transfer_propagatesSameIdValidationFromEjb() {
        TransferRequest req = TestFixtures.transferRequest(1L, 1L, new BigDecimal("10.00"));
        doThrow(new IllegalArgumentException("Origem e destino não podem ser iguais"))
                .when(beneficioEjbService).transfer(1L, 1L, new BigDecimal("10.00"));

        assertThatThrownBy(() -> service.transfer(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não podem ser iguais");

        verify(repository, never()).save(any());
    }

    @Test
    void transfer_propagatesFromNotFoundFromEjb() {
        TransferRequest req = TestFixtures.transferRequest(1L, 2L, new BigDecimal("10.00"));
        doThrow(new EntityNotFoundException("Benefício não encontrado: 1"))
                .when(beneficioEjbService).transfer(1L, 2L, new BigDecimal("10.00"));

        assertThatThrownBy(() -> service.transfer(req))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("1");
    }

    @Test
    void transfer_propagatesToNotFoundFromEjb() {
        TransferRequest req = TestFixtures.transferRequest(1L, 2L, new BigDecimal("10.00"));
        doThrow(new EntityNotFoundException("Benefício não encontrado: 2"))
                .when(beneficioEjbService).transfer(1L, 2L, new BigDecimal("10.00"));

        assertThatThrownBy(() -> service.transfer(req))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("2");
    }

    @Test
    @DisplayName("transfer com valor exatamente igual ao saldo é permitido")
    void transfer_exactBalance_delegatesToEjb() {
        TransferRequest req = TestFixtures.transferRequest(1L, 2L, new BigDecimal("1000.00"));

        service.transfer(req);

        verify(beneficioEjbService).transfer(1L, 2L, new BigDecimal("1000.00"));
    }
}
