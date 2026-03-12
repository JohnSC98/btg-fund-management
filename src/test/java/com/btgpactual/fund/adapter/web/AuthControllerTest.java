package com.btgpactual.fund.adapter.web;

import com.btgpactual.fund.adapter.web.dto.LoginRequest;
import com.btgpactual.fund.adapter.web.dto.LoginResponse;
import com.btgpactual.fund.adapter.web.dto.RegisterRequest;
import com.btgpactual.fund.adapter.web.security.JwtService;
import com.btgpactual.fund.domain.exception.AuthenticationException;
import com.btgpactual.fund.domain.model.User;
import com.btgpactual.fund.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthController authController;

    private static final String EMAIL = "test@example.com";
    private static final String PASSWORD = "password123";

    @Test
    @DisplayName("login throws AuthenticationException when credentials are invalid")
    void loginInvalidCredentials() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authController.login(new LoginRequest(EMAIL, PASSWORD)))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Credenciales");
    }

    @Test
    @DisplayName("login returns token and role for valid credentials")
    void loginSuccess() {
        User user = User.builder()
                .id("user-1")
                .email(EMAIL)
                .passwordHash("encoded-password")
                .balance(new BigDecimal("500000"))
                .role("USER")
                .build();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(PASSWORD, "encoded-password")).thenReturn(true);
        when(jwtService.generateToken("user-1", "USER")).thenReturn("jwt-token");

        var response = authController.login(new LoginRequest(EMAIL, PASSWORD));
        LoginResponse body = response.getBody();

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(body).isNotNull();
        assertThat(body.token()).isEqualTo("jwt-token");
        assertThat(body.role()).isEqualTo("USER");
    }

    @Test
    @DisplayName("login throws AuthenticationException when password does not match")
    void loginWrongPassword() {
        User user = User.builder()
                .id("user-1")
                .email(EMAIL)
                .passwordHash("encoded-password")
                .balance(new BigDecimal("500000"))
                .role("USER")
                .build();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(PASSWORD, "encoded-password")).thenReturn(false);

        assertThatThrownBy(() -> authController.login(new LoginRequest(EMAIL, PASSWORD)))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Credenciales");
    }

    @Test
    @DisplayName("login defaults role to USER when stored role is null")
    void loginNullRoleDefaultsToUser() {
        User user = User.builder()
                .id("user-1")
                .email(EMAIL)
                .passwordHash("encoded-password")
                .balance(new BigDecimal("500000"))
                .role(null)
                .build();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(PASSWORD, "encoded-password")).thenReturn(true);
        when(jwtService.generateToken("user-1", "USER")).thenReturn("jwt-token");

        var response = authController.login(new LoginRequest(EMAIL, PASSWORD));

        assertThat(response.getBody().role()).isEqualTo("USER");
    }

    @Test
    @DisplayName("register throws IllegalStateException when email already exists")
    void registerDuplicateEmail() {
        User existing = User.builder()
                .id("user-1")
                .email(EMAIL)
                .passwordHash("hash")
                .balance(new BigDecimal("500000"))
                .role("USER")
                .build();

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> authController.register(new RegisterRequest(EMAIL, PASSWORD)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("email");
    }

    @Test
    @DisplayName("register returns token and role when data is valid")
    void registerSuccess() {
        ReflectionTestUtils.setField(authController, "initialBalance", new BigDecimal("500000"));

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(PASSWORD)).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(any(), any())).thenReturn("jwt-token");

        var response = authController.register(new RegisterRequest(EMAIL, PASSWORD));
        LoginResponse body = response.getBody();

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(body).isNotNull();
        assertThat(body.token()).isEqualTo("jwt-token");
        assertThat(body.role()).isEqualTo("USER");
    }
}