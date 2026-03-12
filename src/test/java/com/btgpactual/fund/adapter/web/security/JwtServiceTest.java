package com.btgpactual.fund.adapter.web.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "test-secret-at-least-256-bits-required-for-hs256-algorithm";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, 3_600_000L);
    }

    @Test
    @DisplayName("generateToken builds a non-blank JWT")
    void generateToken_nonBlank() {
        String token = jwtService.generateToken("user-1", "USER");
        assertThat(token).isNotBlank().contains(".");
    }

    @Test
    @DisplayName("extractUserId returns the subject embedded in the token")
    void extractUserId_returnsSubject() {
        String token = jwtService.generateToken("user-42", "ADMIN");
        assertThat(jwtService.extractUserId(token)).isEqualTo("user-42");
    }

    @Test
    @DisplayName("isTokenValid returns true for matching user and non-expired token")
    void isTokenValid_trueForValidToken() {
        String token = jwtService.generateToken("user-1", "USER");
        UserDetails details = new User("user-1", "", Collections.emptyList());

        assertThat(jwtService.isTokenValid(token, details)).isTrue();
    }

    @Test
    @DisplayName("isTokenValid returns false when userId does not match")
    void isTokenValid_falseForWrongUser() {
        String token = jwtService.generateToken("user-1", "USER");
        UserDetails details = new User("user-99", "", Collections.emptyList());

        assertThat(jwtService.isTokenValid(token, details)).isFalse();
    }

    @Test
    @DisplayName("expired token raises exception during validation")
    void expiredToken_throwsException() {
        JwtService shortLived = new JwtService(SECRET, -1L);
        String token = shortLived.generateToken("user-1", "USER");
        UserDetails details = new User("user-1", "", Collections.emptyList());

        assertThatThrownBy(() -> jwtService.isTokenValid(token, details))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("different user IDs produce different tokens")
    void differentUsers_differentTokens() {
        String t1 = jwtService.generateToken("user-1", "USER");
        String t2 = jwtService.generateToken("user-2", "USER");
        assertThat(t1).isNotEqualTo(t2);
    }
}
