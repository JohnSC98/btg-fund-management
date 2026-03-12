package com.btgpactual.fund.adapter.notification;

import com.btgpactual.fund.domain.model.User;
import com.btgpactual.fund.domain.port.NotificationSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationSender implements NotificationSender {

    @Override
    public void sendSubscriptionConfirmation(User user, String fundName) {
        String email = user.getNotificationPreference() != null ? user.getNotificationPreference().getEmail() : null;
        if (email == null) return;
        log.info("[EMAIL] Enviando confirmación de suscripción al fondo {} a {}", fundName, email);
        // Integrate with SES, SendGrid, etc.
    }

    @Override
    public void sendUnsubscriptionConfirmation(User user, String fundName) {
        String email = user.getNotificationPreference() != null ? user.getNotificationPreference().getEmail() : null;
        if (email == null) return;
        log.info("[EMAIL] Enviando confirmación de cancelación del fondo {} a {}", fundName, email);
    }

    @Override
    public boolean supports(User.NotificationPreference preference) {
        return preference != null && preference.getChannel() == User.NotificationChannel.EMAIL;
    }
}
