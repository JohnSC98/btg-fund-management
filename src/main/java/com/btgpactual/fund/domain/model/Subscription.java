package com.btgpactual.fund.domain.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@Document(collection = "subscriptions")
@CompoundIndex(name = "user_fund_idx", def = "{'userId': 1, 'fundId': 1}", unique = true)
public class Subscription {

    @Id
    private String id;

    private String userId;

    private String fundId;

    private String fundCode;

    private String fundName;

    @Builder.Default
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    private Instant subscribedAt;

    public enum SubscriptionStatus {
        ACTIVE,
        CANCELLED
    }
}
