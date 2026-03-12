package com.btgpactual.fund.domain.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@Document(collection = "transactions")
public class Transaction {

    @Id
    private String id;

    @Indexed
    private String userId;

    @Builder.Default
    private String transactionId = UUID.randomUUID().toString();

    private TransactionType type;

    private String fundId;

    private String fundCode;

    private String fundName;

    private BigDecimal amount;

    private BigDecimal balanceAfter;

    private String description;

    private Instant createdAt;

    public enum TransactionType {
        SUBSCRIPTION,
        UNSUBSCRIPTION,
        INITIAL_BALANCE
    }
}
