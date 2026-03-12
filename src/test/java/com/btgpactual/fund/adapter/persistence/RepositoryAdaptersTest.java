package com.btgpactual.fund.adapter.persistence;

import com.btgpactual.fund.adapter.persistence.mongo.*;
import com.btgpactual.fund.domain.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryAdaptersTest {

    @Nested
    @DisplayName("FundRepositoryAdapter")
    class FundAdapterTests {
        @Mock MongoFundRepository mongoRepo;
        @InjectMocks FundRepositoryAdapter adapter;

        @Test
        void findById_delegates() {
            Fund fund = Fund.builder().id("f1").code("CODE").name("Name")
                    .minAmount(BigDecimal.TEN).category(Fund.FundCategory.FIC).build();
            when(mongoRepo.findById("f1")).thenReturn(Optional.of(fund));
            assertThat(adapter.findById("f1")).contains(fund);
        }

        @Test
        void findByCode_delegates() {
            Fund fund = Fund.builder().id("f1").code("DEUDAPRIVADA").name("Deuda Privada")
                    .minAmount(BigDecimal.TEN).category(Fund.FundCategory.FIC).build();
            when(mongoRepo.findByCode("DEUDAPRIVADA")).thenReturn(Optional.of(fund));
            assertThat(adapter.findByCode("DEUDAPRIVADA")).contains(fund);
        }

        @Test
        void findAll_delegates() {
            when(mongoRepo.findAll()).thenReturn(List.of());
            assertThat(adapter.findAll()).isEmpty();
        }

        @Test
        void save_delegates() {
            Fund fund = Fund.builder().id("f1").code("CODE").name("Name")
                    .minAmount(BigDecimal.TEN).category(Fund.FundCategory.FPV).build();
            when(mongoRepo.save(fund)).thenReturn(fund);
            assertThat(adapter.save(fund)).isEqualTo(fund);
        }
    }

    @Nested
    @DisplayName("SubscriptionRepositoryAdapter")
    class SubscriptionAdapterTests {
        @Mock MongoSubscriptionRepository mongoRepo;
        @InjectMocks SubscriptionRepositoryAdapter adapter;

        @Test
        void findById_delegates() {
            Subscription sub = Subscription.builder().id("s1").build();
            when(mongoRepo.findById("s1")).thenReturn(Optional.of(sub));
            assertThat(adapter.findById("s1")).contains(sub);
        }

        @Test
        void findByUserIdAndFundId_delegates() {
            Subscription sub = Subscription.builder().id("s1").userId("u1").fundId("f1").build();
            when(mongoRepo.findByUserIdAndFundId("u1", "f1")).thenReturn(Optional.of(sub));
            assertThat(adapter.findByUserIdAndFundId("u1", "f1")).contains(sub);
        }

        @Test
        void findByUserId_delegates() {
            Subscription sub = Subscription.builder().id("s1").userId("u1").build();
            when(mongoRepo.findByUserId("u1")).thenReturn(List.of(sub));
            assertThat(adapter.findByUserId("u1")).containsExactly(sub);
        }

        @Test
        void save_delegates() {
            Subscription sub = Subscription.builder().id("s1").build();
            when(mongoRepo.save(sub)).thenReturn(sub);
            assertThat(adapter.save(sub)).isEqualTo(sub);
        }
    }

    @Nested
    @DisplayName("UserRepositoryAdapter")
    class UserAdapterTests {
        @Mock MongoUserRepository mongoRepo;
        @InjectMocks UserRepositoryAdapter adapter;

        @Test
        void findById_delegates() {
            User user = User.builder().id("u1").email("a@b.com").build();
            when(mongoRepo.findById("u1")).thenReturn(Optional.of(user));
            assertThat(adapter.findById("u1")).contains(user);
        }

        @Test
        void findByEmail_delegates() {
            User user = User.builder().id("u1").email("a@b.com").build();
            when(mongoRepo.findByEmail("a@b.com")).thenReturn(Optional.of(user));
            assertThat(adapter.findByEmail("a@b.com")).contains(user);
        }

        @Test
        void save_delegates() {
            User user = User.builder().id("u1").email("a@b.com").build();
            when(mongoRepo.save(user)).thenReturn(user);
            assertThat(adapter.save(user)).isEqualTo(user);
        }
    }

    @Nested
    @DisplayName("TransactionRepositoryAdapter")
    class TransactionAdapterTests {
        @Mock MongoTransactionRepository mongoRepo;
        @InjectMocks TransactionRepositoryAdapter adapter;

        @Test
        void save_delegates() {
            Transaction tx = Transaction.builder().id("t1").userId("u1").build();
            when(mongoRepo.save(tx)).thenReturn(tx);
            assertThat(adapter.save(tx)).isEqualTo(tx);
        }

        @Test
        void findByUserIdOrderByCreatedAtDesc_delegatesWithPageRequest() {
            Transaction tx = Transaction.builder().id("t1").userId("u1")
                    .type(Transaction.TransactionType.SUBSCRIPTION)
                    .createdAt(Instant.now()).build();
            when(mongoRepo.findByUserIdOrderByCreatedAtDesc("u1", PageRequest.of(0, 10)))
                    .thenReturn(List.of(tx));
            assertThat(adapter.findByUserIdOrderByCreatedAtDesc("u1", 10)).containsExactly(tx);
            verify(mongoRepo).findByUserIdOrderByCreatedAtDesc("u1", PageRequest.of(0, 10));
        }
    }
}
