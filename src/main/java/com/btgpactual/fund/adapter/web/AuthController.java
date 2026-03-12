package com.btgpactual.fund.adapter.web;

import com.btgpactual.fund.adapter.web.dto.LoginRequest;
import com.btgpactual.fund.adapter.web.dto.LoginResponse;
import com.btgpactual.fund.adapter.web.dto.RegisterRequest;
import com.btgpactual.fund.adapter.web.security.JwtService;
import com.btgpactual.fund.domain.exception.AuthenticationException;
import com.btgpactual.fund.domain.model.User;
import com.btgpactual.fund.domain.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${app.initial-balance:500000}")
    private BigDecimal initialBalance;

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalStateException("El email ya está registrado");
        }
        User user = User.builder()
                .id(UUID.randomUUID().toString())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .balance(initialBalance)
                .role("USER")
                .build();
        user = userRepository.save(user);
        String token = jwtService.generateToken(user.getId(), user.getRole());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new LoginResponse(token, user.getId(), user.getRole()));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new AuthenticationException("Credenciales inválidas"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new AuthenticationException("Credenciales inválidas");
        }
        String role = user.getRole() != null ? user.getRole() : "USER";
        String token = jwtService.generateToken(user.getId(), role);
        return ResponseEntity.ok(new LoginResponse(token, user.getId(), role));
    }
}
