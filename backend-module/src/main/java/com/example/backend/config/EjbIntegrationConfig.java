package com.example.backend.config;

import jakarta.persistence.EntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.ejb.services.BeneficioEjbService;

@Configuration
public class EjbIntegrationConfig {

    @Bean
    public BeneficioEjbService beneficioEjbService(EntityManager entityManager) {
        return new BeneficioEjbService(entityManager);
    }
}
