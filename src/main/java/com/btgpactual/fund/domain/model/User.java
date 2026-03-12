package com.btgpactual.fund.domain.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Data
@Builder
@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private String passwordHash;

    @Builder.Default
    private BigDecimal balance = new BigDecimal("500000"); // COP initial balance

    private String role; // USER, ADMIN

    private NotificationPreference notificationPreference;

    @Data
    @Builder
    public static class NotificationPreference {
        private NotificationChannel channel; // EMAIL, SMS
        private String email;
        private String phoneNumber;
    }

    public enum NotificationChannel {
        EMAIL,
        SMS
    }
}
