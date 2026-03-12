package com.btgpactual.fund.adapter.web.dto;

import com.btgpactual.fund.domain.model.Subscription;

import java.time.Instant;

public record SubscriptionResponse(
        String id,
        String fundCode,
        String fundName,
        String status,
        Instant subscribedAt
) {
    public static SubscriptionResponse from(Subscription s) {
        return new SubscriptionResponse(
                s.getId(),
                s.getFundCode(),
                s.getFundName(),
                s.getStatus().name(),
                s.getSubscribedAt()
        );
    }
}
