package com.btgpactual.fund.adapter.web;

import com.btgpactual.fund.adapter.web.dto.SubscribeRequest;
import com.btgpactual.fund.application.service.SubscriptionService;
import com.btgpactual.fund.domain.model.Fund;
import com.btgpactual.fund.domain.model.Subscription;
import com.btgpactual.fund.domain.model.Transaction;
import com.btgpactual.fund.domain.repository.FundRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FundControllerTest {

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private FundRepository fundRepository;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private FundController fundController;

    private static final String USER_ID = "user-1";

    @BeforeEach
    void setUp() {
        lenient().when(userDetails.getUsername()).thenReturn(USER_ID);
    }

    @Test
    @DisplayName("listFunds returns mapped fund list")
    void listFunds_returnsMappedList() {
        Fund fund = Fund.builder()
                .id("f1").code("DEUDAPRIVADA").name("Deuda Privada")
                .minAmount(new BigDecimal("50000")).category(Fund.FundCategory.FIC)
                .build();
        when(fundRepository.findAll()).thenReturn(List.of(fund));

        var response = fundController.listFunds();

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).code()).isEqualTo("DEUDAPRIVADA");
        assertThat(response.getBody().get(0).category()).isEqualTo("FIC");
        assertThat(response.getBody().get(0).minAmount()).isEqualByComparingTo("50000");
    }

    @Test
    @DisplayName("listFunds returns empty list when no funds")
    void listFunds_empty() {
        when(fundRepository.findAll()).thenReturn(List.of());

        var response = fundController.listFunds();

        assertThat(response.getBody()).isEmpty();
    }

    @Test
    @DisplayName("listSubscriptions returns subscriptions for authenticated user")
    void listSubscriptions_returnsUserSubscriptions() {
        Subscription sub = Subscription.builder()
                .id("sub-1").fundCode("DEUDAPRIVADA").fundName("Deuda Privada")
                .status(Subscription.SubscriptionStatus.ACTIVE).subscribedAt(Instant.now())
                .build();
        when(subscriptionService.getSubscriptions(USER_ID)).thenReturn(List.of(sub));

        var response = fundController.listSubscriptions(userDetails);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).fundCode()).isEqualTo("DEUDAPRIVADA");
        assertThat(response.getBody().get(0).status()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("listSubscriptions includes cancelled subscriptions")
    void listSubscriptions_includesCancelled() {
        Subscription sub = Subscription.builder()
                .id("sub-2").fundCode("FDO-ACCIONES").fundName("Fondo Acciones")
                .status(Subscription.SubscriptionStatus.CANCELLED).subscribedAt(Instant.now())
                .build();
        when(subscriptionService.getSubscriptions(USER_ID)).thenReturn(List.of(sub));

        var response = fundController.listSubscriptions(userDetails);

        assertThat(response.getBody().get(0).status()).isEqualTo("CANCELLED");
    }

    @Test
    @DisplayName("subscribe returns 201 with subscription details")
    void subscribe_returnsCreated() {
        Subscription sub = Subscription.builder()
                .id("sub-1").fundCode("DEUDAPRIVADA").fundName("Deuda Privada")
                .status(Subscription.SubscriptionStatus.ACTIVE).subscribedAt(Instant.now())
                .build();
        when(subscriptionService.subscribe(USER_ID, "DEUDAPRIVADA")).thenReturn(sub);

        var response = fundController.subscribe(new SubscribeRequest("DEUDAPRIVADA"), userDetails);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo("sub-1");
        assertThat(response.getBody().fundCode()).isEqualTo("DEUDAPRIVADA");
    }

    @Test
    @DisplayName("unsubscribe returns 204 No Content")
    void unsubscribe_returnsNoContent() {
        var response = fundController.unsubscribe("sub-1", userDetails);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(subscriptionService).unsubscribe(USER_ID, "sub-1");
    }

    @Test
    @DisplayName("getHistory returns transaction list")
    void getHistory_returnsList() {
        Transaction tx = Transaction.builder()
                .transactionId("tx-1").userId(USER_ID)
                .type(Transaction.TransactionType.SUBSCRIPTION)
                .fundCode("DEUDAPRIVADA").fundName("Deuda Privada")
                .amount(new BigDecimal("50000")).balanceAfter(new BigDecimal("450000"))
                .description("Apertura del fondo Deuda Privada").createdAt(Instant.now())
                .build();
        when(subscriptionService.getTransactionHistory(USER_ID, 10)).thenReturn(List.of(tx));

        var response = fundController.getHistory(10, userDetails);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).transactionId()).isEqualTo("tx-1");
        assertThat(response.getBody().get(0).type()).isEqualTo("SUBSCRIPTION");
    }

    @Test
    @DisplayName("getHistory caps limit at 100")
    void getHistory_capsLimitAt100() {
        when(subscriptionService.getTransactionHistory(USER_ID, 100)).thenReturn(List.of());

        fundController.getHistory(200, userDetails);

        verify(subscriptionService).getTransactionHistory(USER_ID, 100);
    }
}
