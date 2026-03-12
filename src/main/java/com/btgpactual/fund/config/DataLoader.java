package com.btgpactual.fund.config;

import com.btgpactual.fund.domain.model.Fund;
import com.btgpactual.fund.domain.model.User;
import com.btgpactual.fund.domain.repository.FundRepository;
import com.btgpactual.fund.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final FundRepository fundRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final List<Fund> SEED_FUNDS = List.of(
            fund("FPV_BTG_PACTUAL_RECAUDADORA", "FPV BTG Pactual Recaudadora", "75000", Fund.FundCategory.FPV),
            fund("FPV_BTG_PACTUAL_ECOPETROL", "FPV BTG Pactual Ecopetrol", "125000", Fund.FundCategory.FPV),
            fund("DEUDAPRIVADA", "Deuda Privada", "50000", Fund.FundCategory.FIC),
            fund("FDO-ACCIONES", "Fondo Acciones", "250000", Fund.FundCategory.FIC),
            fund("FPV_BTG_PACTUAL_DINAMICA", "FPV BTG Pactual Dinámica", "100000", Fund.FundCategory.FPV)
    );

    private static Fund fund(String code, String name, String min, Fund.FundCategory cat) {
        return Fund.builder()
                .id(UUID.randomUUID().toString())
                .code(code)
                .name(name)
                .minAmount(new BigDecimal(min))
                .category(cat)
                .build();
    }

    private User user(String email, String phone, String role, User.NotificationChannel channel) {
        return User.builder()
                .id(UUID.randomUUID().toString())
                .email(email)
                .passwordHash(passwordEncoder.encode("password123"))
                .balance(new BigDecimal("500000"))
                .role(role)
                .notificationPreference(User.NotificationPreference.builder()
                        .channel(channel)
                        .email(email)
                        .phoneNumber(phone)
                        .build())
                .build();
    }

    @Override
    public void run(String... args) {
        // Seed funds
        for (Fund f : SEED_FUNDS) {
            if (fundRepository.findByCode(f.getCode()).isEmpty()) {
                fundRepository.save(f);
                log.info("Fund seeded: {}", f.getCode());
            }
        }

        // Seed users
        List<User> seedUsers = List.of(
                user("carlos.martinez@example.com", "+573001234567", "USER", User.NotificationChannel.EMAIL),
                user("maria.lopez@example.com", "+573009876543", "USER", User.NotificationChannel.SMS),
                user("andres.garcia@example.com", "+573005551234", "USER", User.NotificationChannel.EMAIL),
                user("laura.rodriguez@example.com", "+573007778899", "USER", User.NotificationChannel.EMAIL),
                user("admin@btgpactual.com", "+573000000000", "ADMIN", User.NotificationChannel.EMAIL)
        );

        for (User u : seedUsers) {
            if (userRepository.findByEmail(u.getEmail()).isEmpty()) {
                userRepository.save(u);
                log.info("User seeded: {}", u.getEmail());
            }
        }
    }
}
