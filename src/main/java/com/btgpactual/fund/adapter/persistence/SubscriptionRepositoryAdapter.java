package com.btgpactual.fund.adapter.persistence;

import com.btgpactual.fund.adapter.persistence.mongo.MongoSubscriptionRepository;
import com.btgpactual.fund.domain.model.Subscription;
import com.btgpactual.fund.domain.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SubscriptionRepositoryAdapter implements SubscriptionRepository {

    private final MongoSubscriptionRepository mongoRepository;

    @Override
    public Optional<Subscription> findById(String id) {
        return mongoRepository.findById(id);
    }

    @Override
    public Optional<Subscription> findByUserIdAndFundId(String userId, String fundId) {
        return mongoRepository.findByUserIdAndFundId(userId, fundId);
    }

    @Override
    public List<Subscription> findByUserId(String userId) {
        return mongoRepository.findByUserId(userId);
    }

    @Override
    public Subscription save(Subscription subscription) {
        return mongoRepository.save(subscription);
    }
}
