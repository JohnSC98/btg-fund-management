package com.btgpactual.fund.adapter.persistence.mongo;

import com.btgpactual.fund.domain.model.Fund;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MongoFundRepository extends MongoRepository<Fund, String> {

    Optional<Fund> findByCode(String code);
}
