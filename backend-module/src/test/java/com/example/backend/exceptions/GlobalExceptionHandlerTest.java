package com.example.backend.exceptions;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNotFound_returns404WithBody() {
        EntityNotFoundException ex = new EntityNotFoundException("Benefício não encontrado: 99");

        ResponseEntity<Map<String, Object>> response = handler.handleNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(404);
        assertThat(response.getBody().get("error")).isEqualTo("Benefício não encontrado: 99");
        assertThat(response.getBody().get("timestamp")).isNotNull();
    }

    @Test
    void handleIllegalArgument_returns400WithBody() {
        IllegalArgumentException ex = new IllegalArgumentException("Saldo insuficiente");

        ResponseEntity<Map<String, Object>> response = handler.handleIllegalArgument(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("status")).isEqualTo(400);
        assertThat(response.getBody().get("error")).isEqualTo("Saldo insuficiente");
    }

    @Test
    void handleValidation_concatenaErrosDeCampo() {
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "nome", "Nome é obrigatório."));
        bindingResult.addError(new FieldError("request", "valor", "Valor deve ser maior que zero."));

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<Map<String, Object>> response = handler.handleValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        String error = (String) response.getBody().get("error");
        assertThat(error).contains("nome: Nome é obrigatório.");
        assertThat(error).contains("valor: Valor deve ser maior que zero.");
    }
}
