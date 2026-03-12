package com.btgpactual.fund.adapter.notification;

import com.btgpactual.fund.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

class NotificationSenderTest {

    private EmailNotificationSender emailSender;
    private SmsNotificationSender smsSender;

    @BeforeEach
    void setUp() {
        emailSender = new EmailNotificationSender();
        smsSender = new SmsNotificationSender();
    }

    private User userWithEmail(String email) {
        return User.builder().id("u1").email(email)
                .balance(BigDecimal.ZERO).role("USER")
                .notificationPreference(User.NotificationPreference.builder()
                        .channel(User.NotificationChannel.EMAIL).email(email).build())
                .build();
    }

    private User userWithPhone(String phone) {
        return User.builder().id("u2").email("a@b.com")
                .balance(BigDecimal.ZERO).role("USER")
                .notificationPreference(User.NotificationPreference.builder()
                        .channel(User.NotificationChannel.SMS).phoneNumber(phone).build())
                .build();
    }

    private User userWithNullPreference() {
        return User.builder().id("u3").email("c@d.com")
                .balance(BigDecimal.ZERO).role("USER")
                .notificationPreference(null)
                .build();
    }

    @Nested
    @DisplayName("EmailNotificationSender")
    class EmailSenderTests {

        @Test
        @DisplayName("supports returns true for EMAIL channel")
        void supports_trueForEmail() {
            User.NotificationPreference pref = User.NotificationPreference.builder()
                    .channel(User.NotificationChannel.EMAIL).build();
            assertThat(emailSender.supports(pref)).isTrue();
        }

        @Test
        @DisplayName("supports returns false for SMS channel")
        void supports_falseForSms() {
            User.NotificationPreference pref = User.NotificationPreference.builder()
                    .channel(User.NotificationChannel.SMS).build();
            assertThat(emailSender.supports(pref)).isFalse();
        }

        @Test
        @DisplayName("supports returns false for null preference")
        void supports_falseForNull() {
            assertThat(emailSender.supports(null)).isFalse();
        }

        @Test
        @DisplayName("sendSubscriptionConfirmation does not throw for valid email")
        void sendSubscription_doesNotThrow() {
            User user = userWithEmail("carlos@btg.com");
            assertThatNoException().isThrownBy(
                    () -> emailSender.sendSubscriptionConfirmation(user, "Deuda Privada"));
        }

        @Test
        @DisplayName("sendSubscriptionConfirmation skips when email is null")
        void sendSubscription_skipsNullEmail() {
            User user = userWithNullPreference();
            assertThatNoException().isThrownBy(
                    () -> emailSender.sendSubscriptionConfirmation(user, "Deuda Privada"));
        }

        @Test
        @DisplayName("sendUnsubscriptionConfirmation does not throw for valid email")
        void sendUnsubscription_doesNotThrow() {
            User user = userWithEmail("carlos@btg.com");
            assertThatNoException().isThrownBy(
                    () -> emailSender.sendUnsubscriptionConfirmation(user, "Deuda Privada"));
        }

        @Test
        @DisplayName("sendUnsubscriptionConfirmation skips when email is null")
        void sendUnsubscription_skipsNullEmail() {
            User user = userWithNullPreference();
            assertThatNoException().isThrownBy(
                    () -> emailSender.sendUnsubscriptionConfirmation(user, "Deuda Privada"));
        }
    }

    @Nested
    @DisplayName("SmsNotificationSender")
    class SmsSenderTests {

        @Test
        @DisplayName("supports returns true for SMS channel")
        void supports_trueForSms() {
            User.NotificationPreference pref = User.NotificationPreference.builder()
                    .channel(User.NotificationChannel.SMS).build();
            assertThat(smsSender.supports(pref)).isTrue();
        }

        @Test
        @DisplayName("supports returns false for EMAIL channel")
        void supports_falseForEmail() {
            User.NotificationPreference pref = User.NotificationPreference.builder()
                    .channel(User.NotificationChannel.EMAIL).build();
            assertThat(smsSender.supports(pref)).isFalse();
        }

        @Test
        @DisplayName("supports returns false for null preference")
        void supports_falseForNull() {
            assertThat(smsSender.supports(null)).isFalse();
        }

        @Test
        @DisplayName("sendSubscriptionConfirmation does not throw for valid phone")
        void sendSubscription_doesNotThrow() {
            User user = userWithPhone("+573001234567");
            assertThatNoException().isThrownBy(
                    () -> smsSender.sendSubscriptionConfirmation(user, "Deuda Privada"));
        }

        @Test
        @DisplayName("sendSubscriptionConfirmation skips when phone is null")
        void sendSubscription_skipsNullPhone() {
            User user = userWithNullPreference();
            assertThatNoException().isThrownBy(
                    () -> smsSender.sendSubscriptionConfirmation(user, "Deuda Privada"));
        }

        @Test
        @DisplayName("sendUnsubscriptionConfirmation does not throw for valid phone")
        void sendUnsubscription_doesNotThrow() {
            User user = userWithPhone("+573001234567");
            assertThatNoException().isThrownBy(
                    () -> smsSender.sendUnsubscriptionConfirmation(user, "Deuda Privada"));
        }

        @Test
        @DisplayName("sendUnsubscriptionConfirmation skips when phone is null")
        void sendUnsubscription_skipsNullPhone() {
            User user = userWithNullPreference();
            assertThatNoException().isThrownBy(
                    () -> smsSender.sendUnsubscriptionConfirmation(user, "Deuda Privada"));
        }
    }
}
