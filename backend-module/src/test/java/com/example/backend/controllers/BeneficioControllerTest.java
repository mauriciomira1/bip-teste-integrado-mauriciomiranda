package com.example.backend.controllers;

import com.example.backend.TestFixtures;
import com.example.backend.dto.BeneficioRequest;
import com.example.backend.dto.BeneficioResponse;
import com.example.backend.dto.TransferRequest;
import com.example.backend.models.BeneficioEntity;
import com.example.backend.services.BeneficioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BeneficioController.class)
class BeneficioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BeneficioService service;

    @Test
    void list_returns200WithArray() throws Exception {
        BeneficioEntity entity = TestFixtures.beneficio(1L, "A", new BigDecimal("100.00"));
        when(service.findAll()).thenReturn(List.of(BeneficioResponse.from(entity)));

        mockMvc.perform(get("/api/v1/beneficios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].nome").value("A"))
                .andExpect(jsonPath("$[0].valor").value(100.00));
    }

    @Test
    void getById_returns200() throws Exception {
        BeneficioEntity entity = TestFixtures.beneficio(7L, "X", new BigDecimal("42.00"));
        when(service.findById(7L)).thenReturn(BeneficioResponse.from(entity));

        mockMvc.perform(get("/api/v1/beneficios/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.nome").value("X"));
    }

    @Test
    void getById_returns404WhenServiceThrows() throws Exception {
        when(service.findById(99L)).thenThrow(new EntityNotFoundException("Benefício não encontrado: 99"));

        mockMvc.perform(get("/api/v1/beneficios/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value(containsString("99")))
                .andExpect(jsonPath("$.timestamp").value(notNullValue()));
    }

    @Test
    void create_returns201WithBody() throws Exception {
        BeneficioRequest req = TestFixtures.request("Novo", new BigDecimal("300.00"));
        BeneficioEntity entity = TestFixtures.beneficio(5L, "Novo", new BigDecimal("300.00"));
        when(service.create(any(BeneficioRequest.class))).thenReturn(BeneficioResponse.from(entity));

        mockMvc.perform(post("/api/v1/beneficios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.nome").value("Novo"));
    }

    @Test
    void create_returns400WhenNomeBlank() throws Exception {
        BeneficioRequest req = new BeneficioRequest();
        req.setValor(new BigDecimal("100.00"));

        mockMvc.perform(post("/api/v1/beneficios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("nome")));
    }

    @Test
    void create_returns400WhenValorNull() throws Exception {
        BeneficioRequest req = new BeneficioRequest();
        req.setNome("X");

        mockMvc.perform(post("/api/v1/beneficios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("valor")));
    }

    @Test
    void create_returns400WhenValorBelowMin() throws Exception {
        BeneficioRequest req = TestFixtures.request("X", new BigDecimal("0.00"));

        mockMvc.perform(post("/api/v1/beneficios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_returns200() throws Exception {
        BeneficioRequest req = TestFixtures.request("Editado", new BigDecimal("999.00"));
        BeneficioEntity entity = TestFixtures.beneficio(3L, "Editado", new BigDecimal("999.00"));
        when(service.update(eq(3L), any(BeneficioRequest.class))).thenReturn(BeneficioResponse.from(entity));

        mockMvc.perform(put("/api/v1/beneficios/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Editado"))
                .andExpect(jsonPath("$.valor").value(999.00));
    }

    @Test
    void update_returns400OnInvalidPayload() throws Exception {
        BeneficioRequest req = new BeneficioRequest();

        mockMvc.perform(put("/api/v1/beneficios/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_returns404WhenServiceThrows() throws Exception {
        BeneficioRequest req = TestFixtures.request("X", new BigDecimal("1.00"));
        when(service.update(eq(99L), any(BeneficioRequest.class)))
                .thenThrow(new EntityNotFoundException("Benefício não encontrado: 99"));

        mockMvc.perform(put("/api/v1/beneficios/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_returns204() throws Exception {
        doNothing().when(service).delete(1L);

        mockMvc.perform(delete("/api/v1/beneficios/1"))
                .andExpect(status().isNoContent());

        verify(service).delete(1L);
    }

    @Test
    void delete_returns404WhenServiceThrows() throws Exception {
        doThrow(new EntityNotFoundException("Benefício não encontrado: 99"))
                .when(service).delete(99L);

        mockMvc.perform(delete("/api/v1/beneficios/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void transfer_returns204() throws Exception {
        TransferRequest req = TestFixtures.transferRequest(1L, 2L, new BigDecimal("100.00"));
        doNothing().when(service).transfer(any(TransferRequest.class));

        mockMvc.perform(post("/api/v1/beneficios/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNoContent());
    }

    @Test
    void transfer_returns400WhenAmountNull() throws Exception {
        TransferRequest req = new TransferRequest();
        req.setFromId(1L);
        req.setToId(2L);

        mockMvc.perform(post("/api/v1/beneficios/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transfer_returns400WhenFromIdNull() throws Exception {
        TransferRequest req = new TransferRequest();
        req.setToId(2L);
        req.setAmount(new BigDecimal("10.00"));

        mockMvc.perform(post("/api/v1/beneficios/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transfer_returns400WhenServiceThrowsIllegalArgument() throws Exception {
        TransferRequest req = TestFixtures.transferRequest(1L, 2L, new BigDecimal("99999.00"));
        doThrow(new IllegalArgumentException("Saldo insuficiente. Disponível: 100, solicitado: 99999"))
                .when(service).transfer(any(TransferRequest.class));

        mockMvc.perform(post("/api/v1/beneficios/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value(containsString("Saldo insuficiente")));
    }
}
