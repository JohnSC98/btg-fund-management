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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private FundRepository fundRepository;
    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private NotificationSender notificationSender;

    private SubscriptionService subscriptionService;

    private static final String USER_ID = "user-1";
    private static final String FUND_ID = "fund-1";
    private static final String FUND_CODE = "FPV_BTG_PACTUAL_RECAUDADORA";

    private User user;
    private Fund fund;

    @BeforeEach
    void setUp() {
        subscriptionService = new SubscriptionService(
                userRepository, fundRepository, subscriptionRepository, transactionRepository,
                List.of(notificationSender)
        );
        user = User.builder()
                .id(USER_ID)
                .email("test@example.com")
                .balance(new BigDecimal("100000"))
                .role("USER")
                .notificationPreference(User.NotificationPreference.builder()
                        .channel(User.NotificationChannel.SMS)
                        .build())
                .build();
        fund = Fund.builder()
                .id(FUND_ID)
                .code(FUND_CODE)
                .name("FPV BTG Pactual Recaudadora")
                .minAmount(new BigDecimal("75000"))
                .category(Fund.FundCategory.FPV)
                .build();
    }

    @Nested
    @DisplayName("subscribe")
    class Subscribe {

        @Test
        @DisplayName("throws when user not found")
        void userNotFound() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> subscriptionService.subscribe(USER_ID, FUND_CODE))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Usuario no encontrado");
        }

        @Test
        @DisplayName("throws when fund not found")
        void fundNotFound() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(fundRepository.findByCode(FUND_CODE)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> subscriptionService.subscribe(USER_ID, FUND_CODE))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Fondo no encontrado");
        }

        @Test
        @DisplayName("throws InsufficientBalanceException when balance < minAmount")
        void insufficientBalance() {
            user.setBalance(new BigDecimal("50000"));
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(fundRepository.findByCode(FUND_CODE)).thenReturn(Optional.of(fund));

            assertThatThrownBy(() -> subscriptionService.subscribe(USER_ID, FUND_CODE))
                    .isInstanceOf(InsufficientBalanceException.class)
                    .hasMessageContaining("No tiene saldo disponible para vincularse al fondo FPV BTG Pactual Recaudadora");
        }

        @Test
        @DisplayName("subscribes and updates balance and creates transaction")
        void success() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(fundRepository.findByCode(FUND_CODE)).thenReturn(Optional.of(fund));
            when(subscriptionRepository.findByUserIdAndFundId(USER_ID, FUND_ID)).thenReturn(Optional.empty());
            when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(i -> i.getArgument(0));
            when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
            when(notificationSender.supports(any())).thenReturn(false);

            Subscription result = subscriptionService.subscribe(USER_ID, FUND_CODE);

            assertThat(result.getFundCode()).isEqualTo(FUND_CODE);
            assertThat(result.getStatus()).isEqualTo(Subscription.SubscriptionStatus.ACTIVE);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getBalance()).isEqualByComparingTo(new BigDecimal("25000")); // 100000 - 75000

            ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
            verify(transactionRepository).save(txCaptor.capture());
            assertThat(txCaptor.getValue().getType()).isEqualTo(Transaction.TransactionType.SUBSCRIPTION);
            assertThat(txCaptor.getValue().getAmount()).isEqualByComparingTo(new BigDecimal("75000"));
        }

        @Test
        @DisplayName("throws when already subscribed to the fund")
        void duplicateSubscription() {
            Subscription existingSub = Subscription.builder()
                    .id("sub-existing")
                    .userId(USER_ID)
                    .fundId(FUND_ID)
                    .status(Subscription.SubscriptionStatus.ACTIVE)
                    .build();
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(fundRepository.findByCode(FUND_CODE)).thenReturn(Optional.of(fund));
            when(subscriptionRepository.findByUserIdAndFundId(USER_ID, FUND_ID)).thenReturn(Optional.of(existingSub));

            assertThatThrownBy(() -> subscriptionService.subscribe(USER_ID, FUND_CODE))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Ya está suscrito al fondo");
        }

        @Test
        @DisplayName("succeeds even when notification fails")
        void notificationFailureDoesNotBreakSubscription() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(fundRepository.findByCode(FUND_CODE)).thenReturn(Optional.of(fund));
            when(subscriptionRepository.findByUserIdAndFundId(USER_ID, FUND_ID)).thenReturn(Optional.empty());
            when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(i -> i.getArgument(0));
            when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
            when(notificationSender.supports(any())).thenReturn(true);
            org.mockito.Mockito.doThrow(new RuntimeException("SMS gateway error"))
                    .when(notificationSender).sendSubscriptionConfirmation(any(), any());

            Subscription result = subscriptionService.subscribe(USER_ID, FUND_CODE);

            assertThat(result.getFundCode()).isEqualTo(FUND_CODE);
            assertThat(result.getStatus()).isEqualTo(Subscription.SubscriptionStatus.ACTIVE);
        }

        @Test
        @DisplayName("reactivates a cancelled subscription instead of creating a new one")
        void reactivatesCancelledSubscription() {
            Subscription cancelled = Subscription.builder()
                    .id("sub-existing")
                    .userId(USER_ID)
                    .fundId(FUND_ID)
                    .fundCode(FUND_CODE)
                    .fundName("FPV BTG Pactual Recaudadora")
                    .status(Subscription.SubscriptionStatus.CANCELLED)
                    .build();
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(fundRepository.findByCode(FUND_CODE)).thenReturn(Optional.of(fund));
            when(subscriptionRepository.findByUserIdAndFundId(USER_ID, FUND_ID)).thenReturn(Optional.of(cancelled));
            when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(i -> i.getArgument(0));
            when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
            when(notificationSender.supports(any())).thenReturn(false);

            Subscription result = subscriptionService.subscribe(USER_ID, FUND_CODE);

            assertThat(result.getId()).isEqualTo("sub-existing");
            assertThat(result.getStatus()).isEqualTo(Subscription.SubscriptionStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("unsubscribe")
    class Unsubscribe {

        @Test
        @DisplayName("throws when subscription not found")
        void subscriptionNotFound() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(subscriptionRepository.findById("sub-1")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> subscriptionService.unsubscribe(USER_ID, "sub-1"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("throws when subscription belongs to another user")
        void ownershipViolation() {
            Subscription sub = Subscription.builder()
                    .id("sub-1")
                    .userId("another-user")
                    .fundId(FUND_ID)
                    .fundCode(FUND_CODE)
                    .status(Subscription.SubscriptionStatus.ACTIVE)
                    .build();
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(subscriptionRepository.findById("sub-1")).thenReturn(Optional.of(sub));

            assertThatThrownBy(() -> subscriptionService.unsubscribe(USER_ID, "sub-1"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("throws when subscription already cancelled")
        void alreadyCancelled() {
            Subscription sub = Subscription.builder()
                    .id("sub-1")
                    .userId(USER_ID)
                    .fundId(FUND_ID)
                    .fundCode(FUND_CODE)
                    .status(Subscription.SubscriptionStatus.CANCELLED)
                    .build();
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(subscriptionRepository.findById("sub-1")).thenReturn(Optional.of(sub));

            assertThatThrownBy(() -> subscriptionService.unsubscribe(USER_ID, "sub-1"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("ya está cancelada");
        }

        @Test
        @DisplayName("refunds and cancels subscription")
        void success() {
            Subscription sub = Subscription.builder()
                    .id("sub-1")
                    .userId(USER_ID)
                    .fundId(FUND_ID)
                    .fundCode(FUND_CODE)
                    .fundName("FPV BTG Pactual Recaudadora")
                    .status(Subscription.SubscriptionStatus.ACTIVE)
                    .build();
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(subscriptionRepository.findById("sub-1")).thenReturn(Optional.of(sub));
            when(fundRepository.findById(FUND_ID)).thenReturn(Optional.of(fund));
            when(subscriptionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
            when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

            subscriptionService.unsubscribe(USER_ID, "sub-1");

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getBalance()).isEqualByComparingTo(new BigDecimal("175000")); // 100000 + 75000
        }
    }

    @Nested
    @DisplayName("getSubscriptions")
    class GetSubscriptions {

        @Test
        @DisplayName("returns subscriptions for user")
        void returnsSubscriptions() {
            Subscription sub = Subscription.builder()
                    .id("sub-1").userId(USER_ID).fundId(FUND_ID).fundCode(FUND_CODE)
                    .status(Subscription.SubscriptionStatus.ACTIVE)
                    .build();
            when(subscriptionRepository.findByUserId(USER_ID)).thenReturn(List.of(sub));

            List<Subscription> result = subscriptionService.getSubscriptions(USER_ID);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo("sub-1");
        }

        @Test
        @DisplayName("returns empty list when user has no subscriptions")
        void returnsEmptyList() {
            when(subscriptionRepository.findByUserId(USER_ID)).thenReturn(List.of());

            assertThat(subscriptionService.getSubscriptions(USER_ID)).isEmpty();
        }
    }

    @Nested
    @DisplayName("getTransactionHistory")
    class GetHistory {

        @Test
        @DisplayName("returns transactions ordered by date desc")
        void returnsHistory() {
            Transaction t = Transaction.builder()
                    .transactionId("tx-1")
                    .userId(USER_ID)
                    .type(Transaction.TransactionType.SUBSCRIPTION)
                    .amount(new BigDecimal("75000"))
                    .build();
            when(transactionRepository.findByUserIdOrderByCreatedAtDesc(eq(USER_ID), eq(50)))
                    .thenReturn(List.of(t));

            List<Transaction> history = subscriptionService.getTransactionHistory(USER_ID, 50);

            assertThat(history).hasSize(1);
            assertThat(history.get(0).getTransactionId()).isEqualTo("tx-1");
        }
    }
}
