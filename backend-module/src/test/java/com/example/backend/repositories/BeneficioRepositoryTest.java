package com.example.backend.repositories;

import com.example.backend.TestFixtures;
import com.example.backend.models.BeneficioEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BeneficioRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private BeneficioRepository repository;

    @Test
    void findByAtivoTrue_filtraInativos() {
        em.persist(TestFixtures.beneficio("Ativo 1", new BigDecimal("100.00")));
        em.persist(TestFixtures.beneficio("Ativo 2", new BigDecimal("200.00")));
        BeneficioEntity inativo = TestFixtures.beneficio("Inativo", new BigDecimal("300.00"));
        inativo.setAtivo(false);
        em.persist(inativo);
        em.flush();

        List<BeneficioEntity> ativos = repository.findByAtivoTrue();

        assertThat(ativos).hasSize(2);
        assertThat(ativos).extracting(BeneficioEntity::getNome)
                .containsExactlyInAnyOrder("Ativo 1", "Ativo 2");
    }

    @Test
    void version_incrementaEmUpdate() {
        BeneficioEntity entity = em.persistAndFlush(TestFixtures.beneficio("V", new BigDecimal("10.00")));
        Long initialVersion = entity.getVersion();

        entity.setValor(new BigDecimal("20.00"));
        em.persistAndFlush(entity);
        em.refresh(entity);

        assertThat(entity.getVersion()).isGreaterThan(initialVersion);
    }
}
