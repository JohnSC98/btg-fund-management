package com.btgpactual.fund.adapter.web;

import com.btgpactual.fund.adapter.web.dto.ErrorResponse;
import com.btgpactual.fund.domain.exception.AuthenticationException;
import com.btgpactual.fund.domain.exception.InsufficientBalanceException;
import com.btgpactual.fund.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");
    }

    @Test
    @DisplayName("handleResourceNotFound returns 404")
    void resourceNotFound() {
        var ex = new ResourceNotFoundException("Fondo no encontrado");

        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().message()).isEqualTo("Fondo no encontrado");
        assertThat(response.getBody().path()).isEqualTo("/api/test");
    }

    @Test
    @DisplayName("handleInsufficientBalance returns 400")
    void insufficientBalance() {
        var ex = new InsufficientBalanceException("Fondo Acciones");

        ResponseEntity<ErrorResponse> response = handler.handleInsufficientBalance(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().message()).contains("Fondo Acciones");
    }

    @Test
    @DisplayName("handleAuthentication returns 401")
    void authentication() {
        var ex = new AuthenticationException("Credenciales inválidas");

        ResponseEntity<ErrorResponse> response = handler.handleAuthentication(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().status()).isEqualTo(401);
        assertThat(response.getBody().message()).isEqualTo("Credenciales inválidas");
    }

    @Test
    @DisplayName("handleIllegalArgument returns 400")
    void illegalArgument() {
        var ex = new IllegalArgumentException("Argumento inválido");

        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).isEqualTo("Argumento inválido");
    }

    @Test
    @DisplayName("handleIllegalState returns 409")
    void illegalState() {
        var ex = new IllegalStateException("Ya está suscrito al fondo X");

        ResponseEntity<ErrorResponse> response = handler.handleIllegalState(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().status()).isEqualTo(409);
        assertThat(response.getBody().error()).isEqualTo("Conflict");
    }

    @Test
    @DisplayName("handleValidation returns 400 with field errors")
    void validation() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("obj", "email", "no debe estar vacío");
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ErrorResponse> response = handler.handleValidation(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).isEqualTo("Errores de validación");
        assertThat(response.getBody().fieldErrors()).hasSize(1);
        assertThat(response.getBody().fieldErrors().get(0).field()).isEqualTo("email");
    }

    @Test
    @DisplayName("handleGeneric returns 500")
    void generic() {
        var ex = new RuntimeException("error inesperado");

        ResponseEntity<ErrorResponse> response = handler.handleGeneric(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().status()).isEqualTo(500);
    }
}
