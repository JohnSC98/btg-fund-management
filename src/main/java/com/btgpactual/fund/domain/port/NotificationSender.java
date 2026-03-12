package com.btgpactual.fund.domain.port;

import com.btgpactual.fund.domain.model.User;

/**
 * Port for sending notifications (Strategy pattern).
 * Implementations: EmailNotificationSender, SmsNotificationSender.
 */
public interface NotificationSender {

    void sendSubscriptionConfirmation(User user, String fundName);

    void sendUnsubscriptionConfirmation(User user, String fundName);

    boolean supports(User.NotificationPreference preference);
}
