package com.example.backend.repositories;

import com.example.backend.models.BeneficioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BeneficioRepository extends JpaRepository<BeneficioEntity, Long> {

    List<BeneficioEntity> findByAtivoTrue();
}
