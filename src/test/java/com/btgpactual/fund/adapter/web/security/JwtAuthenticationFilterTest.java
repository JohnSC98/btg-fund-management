package com.btgpactual.fund.adapter.web.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private AppUserDetailsService userDetailsService;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain chain;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        chain = new MockFilterChain();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("request without Authorization header passes through without authentication")
    void noHeader_chainContinues() throws Exception {
        filter.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    @DisplayName("request with non-Bearer Authorization passes through without authentication")
    void nonBearerHeader_chainContinues() throws Exception {
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");

        filter.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("valid bearer token sets authentication in SecurityContext")
    void validToken_setsAuthentication() throws Exception {
        String token = "valid.jwt.token";
        UserDetails userDetails = new User("user-1", "", Collections.emptyList());

        request.addHeader("Authorization", "Bearer " + token);
        when(jwtService.extractUserId(token)).thenReturn("user-1");
        when(userDetailsService.loadUserByUsername("user-1")).thenReturn(userDetails);
        when(jwtService.isTokenValid(token, userDetails)).thenReturn(true);

        filter.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("user-1");
    }

    @Test
    @DisplayName("invalid token does not set authentication")
    void invalidToken_noAuthentication() throws Exception {
        String token = "invalid.jwt.token";
        UserDetails userDetails = new User("user-1", "", Collections.emptyList());

        request.addHeader("Authorization", "Bearer " + token);
        when(jwtService.extractUserId(token)).thenReturn("user-1");
        when(userDetailsService.loadUserByUsername("user-1")).thenReturn(userDetails);
        when(jwtService.isTokenValid(token, userDetails)).thenReturn(false);

        filter.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("null userId from token does not call userDetailsService")
    void nullUserId_skipsAuthentication() throws Exception {
        String token = "some.token";
        request.addHeader("Authorization", "Bearer " + token);
        when(jwtService.extractUserId(token)).thenReturn(null);

        filter.doFilterInternal(request, response, chain);

        verifyNoInteractions(userDetailsService);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
