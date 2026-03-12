package com.btgpactual.fund.domain.repository;

import com.btgpactual.fund.domain.model.Subscription;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository {

    Optional<Subscription> findById(String id);

    Optional<Subscription> findByUserIdAndFundId(String userId, String fundId);

    List<Subscription> findByUserId(String userId);

    Subscription save(Subscription subscription);
}
