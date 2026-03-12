package com.btgpactual.fund.adapter.web.dto;

import com.btgpactual.fund.domain.model.Transaction;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(
        String transactionId,
        String type,
        String fundCode,
        String fundName,
        BigDecimal amount,
        BigDecimal balanceAfter,
        String description,
        Instant createdAt
) {
    public static TransactionResponse from(Transaction t) {
        return new TransactionResponse(
                t.getTransactionId(),
                t.getType().name(),
                t.getFundCode(),
                t.getFundName(),
                t.getAmount(),
                t.getBalanceAfter(),
                t.getDescription(),
                t.getCreatedAt()
        );
    }
}
