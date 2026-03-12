package com.btgpactual.fund.adapter.persistence.mongo;

import com.btgpactual.fund.domain.model.Subscription;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MongoSubscriptionRepository extends MongoRepository<Subscription, String> {

    Optional<Subscription> findByUserIdAndFundId(String userId, String fundId);

    List<Subscription> findByUserId(String userId);
}
