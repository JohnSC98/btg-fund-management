package com.btgpactual.fund.adapter.web.security;

import com.btgpactual.fund.domain.model.User;
import com.btgpactual.fund.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AppUserDetailsService service;

    private User buildUser(String id, String email, String role) {
        return User.builder()
                .id(id).email(email).passwordHash("hash")
                .balance(new BigDecimal("500000")).role(role)
                .build();
    }

    @Test
    @DisplayName("loadUserByUsername finds user by id and maps ROLE_USER")
    void loadByUserId_mapsRoleUser() {
        User user = buildUser("user-1", "a@b.com", "USER");
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));

        UserDetails details = service.loadUserByUsername("user-1");

        assertThat(details.getUsername()).isEqualTo("user-1");
        assertThat(details.getAuthorities()).anyMatch(a -> a.getAuthority().equals("ROLE_USER"));
    }

    @Test
    @DisplayName("loadUserByUsername finds user by email when not found by id")
    void loadByEmail_whenNotFoundById() {
        User user = buildUser("user-2", "test@btg.com", "ADMIN");
        when(userRepository.findById("test@btg.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@btg.com")).thenReturn(Optional.of(user));

        UserDetails details = service.loadUserByUsername("test@btg.com");

        assertThat(details.getUsername()).isEqualTo("user-2");
        assertThat(details.getAuthorities()).anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("loadUserByUsername throws UsernameNotFoundException when user absent")
    void notFound_throwsUsernameNotFoundException() {
        when(userRepository.findById("unknown")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("unknown"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Usuario no encontrado");
    }

    @Test
    @DisplayName("loadUserByUsername uses ROLE_USER when role is null")
    void nullRole_defaultsToRoleUser() {
        User user = buildUser("user-3", "c@d.com", null);
        when(userRepository.findById("user-3")).thenReturn(Optional.of(user));

        UserDetails details = service.loadUserByUsername("user-3");

        assertThat(details.getAuthorities()).anyMatch(a -> a.getAuthority().equals("ROLE_USER"));
    }

    @Test
    @DisplayName("loadUserByUsername returns empty password when passwordHash is null")
    void nullPassword_returnsEmptyString() {
        User user = User.builder().id("user-4").email("e@f.com")
                .passwordHash(null).balance(BigDecimal.ZERO).role("USER").build();
        when(userRepository.findById("user-4")).thenReturn(Optional.of(user));

        UserDetails details = service.loadUserByUsername("user-4");

        assertThat(details.getPassword()).isEmpty();
    }
}
