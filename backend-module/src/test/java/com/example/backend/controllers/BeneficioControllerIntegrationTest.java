package com.example.backend.controllers;

import com.example.backend.TestFixtures;
import com.example.backend.dto.BeneficioRequest;
import com.example.backend.dto.TransferRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class BeneficioControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void list_returnsOnlyActiveBeneficios() throws Exception {
        mockMvc.perform(get("/api/v1/beneficios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nome").value("Beneficio A"))
                .andExpect(jsonPath("$[1].nome").value("Beneficio B"));
    }

    @Test
    void getById_returns200ForExisting() throws Exception {
        mockMvc.perform(get("/api/v1/beneficios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Beneficio A"))
                .andExpect(jsonPath("$.valor").value(1000.00));
    }

    @Test
    void getById_returns404WhenMissing() throws Exception {
        mockMvc.perform(get("/api/v1/beneficios/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void create_persistsNewBeneficio() throws Exception {
        BeneficioRequest req = TestFixtures.request("Novo", new BigDecimal("250.00"));

        mockMvc.perform(post("/api/v1/beneficios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Novo"))
                .andExpect(jsonPath("$.ativo").value(true));

        mockMvc.perform(get("/api/v1/beneficios"))
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void create_returns400WhenInvalid() throws Exception {
        BeneficioRequest req = new BeneficioRequest();
        req.setValor(new BigDecimal("100.00"));

        mockMvc.perform(post("/api/v1/beneficios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("nome")));
    }

    @Test
    void update_persistsChanges() throws Exception {
        BeneficioRequest req = TestFixtures.request("Beneficio A Editado", new BigDecimal("1500.00"));

        mockMvc.perform(put("/api/v1/beneficios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Beneficio A Editado"))
                .andExpect(jsonPath("$.valor").value(1500.00));

        mockMvc.perform(get("/api/v1/beneficios/1"))
                .andExpect(jsonPath("$.nome").value("Beneficio A Editado"))
                .andExpect(jsonPath("$.valor").value(1500.00));
    }

    @Test
    void delete_softDeletesBeneficio() throws Exception {
        mockMvc.perform(delete("/api/v1/beneficios/1"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/beneficios"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(2));
    }

    @Test
    void transfer_endToEnd_estadoFinalConsistente() throws Exception {
        TransferRequest req = TestFixtures.transferRequest(1L, 2L, new BigDecimal("100.00"));

        mockMvc.perform(post("/api/v1/beneficios/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/beneficios/1"))
                .andExpect(jsonPath("$.valor").value(900.00));
        mockMvc.perform(get("/api/v1/beneficios/2"))
                .andExpect(jsonPath("$.valor").value(600.00));
    }

    @Test
    void transfer_returns400OnInsufficientBalance() throws Exception {
        TransferRequest req = TestFixtures.transferRequest(1L, 2L, new BigDecimal("99999.00"));

        mockMvc.perform(post("/api/v1/beneficios/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("Saldo insuficiente")));

        // Estado original preservado em caso de falha
        mockMvc.perform(get("/api/v1/beneficios/1"))
                .andExpect(jsonPath("$.valor").value(1000.00));
        mockMvc.perform(get("/api/v1/beneficios/2"))
                .andExpect(jsonPath("$.valor").value(500.00));
    }

    @Test
    void transfer_returns404WhenIdInexistente() throws Exception {
        TransferRequest req = TestFixtures.transferRequest(1L, 9999L, new BigDecimal("10.00"));

        mockMvc.perform(post("/api/v1/beneficios/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    void transfer_returns400WhenSameId() throws Exception {
        TransferRequest req = TestFixtures.transferRequest(1L, 1L, new BigDecimal("10.00"));

        mockMvc.perform(post("/api/v1/beneficios/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("não podem ser iguais")));
    }
}
