package com.btgpactual.fund.application.service;

import com.btgpactual.fund.domain.exception.InsufficientBalanceException;
import com.btgpactual.fund.domain.exception.ResourceNotFoundException;
import com.btgpactual.fund.domain.model.Fund;
import com.btgpactual.fund.domain.model.Subscription;
import com.btgpactual.fund.domain.model.Transaction;
import com.btgpactual.fund.domain.model.User;
import com.btgpactual.fund.domain.port.NotificationSender;
import com.btgpactual.fund.domain.repository.FundRepository;
import com.btgpactual.fund.domain.repository.SubscriptionRepository;
import com.btgpactual.fund.domain.repository.TransactionRepository;
import com.btgpactual.fund.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private final UserRepository userRepository;
    private final FundRepository fundRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final TransactionRepository transactionRepository;
    private final List<NotificationSender> notificationSenders;

    @Transactional
    public Subscription subscribe(String userId, String fundCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        Fund fund = fundRepository.findByCode(fundCode)
                .orElseThrow(() -> new ResourceNotFoundException("Fondo no encontrado: " + fundCode));

        if (user.getBalance().compareTo(fund.getMinAmount()) < 0) {
            throw new InsufficientBalanceException(fund.getName());
        }

        java.util.Optional<Subscription> existingOpt = subscriptionRepository.findByUserIdAndFundId(userId, fund.getId());
        if (existingOpt.isPresent() && existingOpt.get().getStatus() == Subscription.SubscriptionStatus.ACTIVE) {
            throw new IllegalStateException("Ya está suscrito al fondo " + fund.getName());
        }

        BigDecimal minAmount = fund.getMinAmount();
        BigDecimal newBalance = user.getBalance().subtract(minAmount);
        user.setBalance(newBalance);
        userRepository.save(user);

        Subscription subscription;
        if (existingOpt.isPresent()) {
            // Reactivar suscripción cancelada
            subscription = existingOpt.get();
            subscription.setStatus(Subscription.SubscriptionStatus.ACTIVE);
            subscription.setSubscribedAt(Instant.now());
        } else {
            subscription = Subscription.builder()
                    .id(UUID.randomUUID().toString())
                    .userId(userId)
                    .fundId(fund.getId())
                    .fundCode(fund.getCode())
                    .fundName(fund.getName())
                    .status(Subscription.SubscriptionStatus.ACTIVE)
                    .subscribedAt(Instant.now())
                    .build();
        }
        subscription = subscriptionRepository.save(subscription);

        Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID().toString())
                .transactionId(UUID.randomUUID().toString())
                .userId(userId)
                .type(Transaction.TransactionType.SUBSCRIPTION)
                .fundId(fund.getId())
                .fundCode(fund.getCode())
                .fundName(fund.getName())
                .amount(minAmount)
                .balanceAfter(newBalance)
                .description("Suscripción a " + fund.getName())
                .createdAt(Instant.now())
                .build();
        transactionRepository.save(transaction);

        notifySubscription(user, fund.getName());
        return subscription;
    }

    @Transactional
    public void unsubscribe(String userId, String subscriptionId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Suscripción no encontrada"));
        if (!subscription.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Suscripción no encontrada");
        }
        if (subscription.getStatus() == Subscription.SubscriptionStatus.CANCELLED) {
            throw new IllegalStateException("La suscripción ya está cancelada");
        }

        Fund fund = fundRepository.findById(subscription.getFundId())
                .orElseThrow(() -> new ResourceNotFoundException("Fondo no encontrado"));

        BigDecimal refundAmount = fund.getMinAmount();
        BigDecimal newBalance = user.getBalance().add(refundAmount);
        user.setBalance(newBalance);
        userRepository.save(user);

        subscription.setStatus(Subscription.SubscriptionStatus.CANCELLED);
        subscriptionRepository.save(subscription);

        Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID().toString())
                .transactionId(UUID.randomUUID().toString())
                .userId(userId)
                .type(Transaction.TransactionType.UNSUBSCRIPTION)
                .fundId(fund.getId())
                .fundCode(fund.getCode())
                .fundName(fund.getName())
                .amount(refundAmount)
                .balanceAfter(newBalance)
                .description("Cancelación suscripción " + fund.getName())
                .createdAt(Instant.now())
                .build();
        transactionRepository.save(transaction);

        notifyUnsubscription(user, fund.getName());
    }

    public List<Transaction> getTransactionHistory(String userId, int limit) {
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId, limit);
    }

    public List<Subscription> getSubscriptions(String userId) {
        return subscriptionRepository.findByUserId(userId);
    }

    private void notifySubscription(User user, String fundName) {
        sendNotification(user, fundName, sender -> sender.sendSubscriptionConfirmation(user, fundName));
    }

    private void notifyUnsubscription(User user, String fundName) {
        sendNotification(user, fundName, sender -> sender.sendUnsubscriptionConfirmation(user, fundName));
    }

    private void sendNotification(User user, String fundName, java.util.function.Consumer<NotificationSender> send) {
        if (user.getNotificationPreference() == null) return;
        notificationSenders.stream()
                .filter(s -> s.supports(user.getNotificationPreference()))
                .findFirst()
                .ifPresent(sender -> {
                    try {
                        send.accept(sender);
                    } catch (Exception e) {
                        log.warn("Error sending notification: {}", e.getMessage());
                    }
                });
    }
}
