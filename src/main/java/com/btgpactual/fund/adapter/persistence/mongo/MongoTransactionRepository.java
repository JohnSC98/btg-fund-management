package com.btgpactual.fund.adapter.persistence.mongo;

import com.btgpactual.fund.domain.model.Transaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MongoTransactionRepository extends MongoRepository<Transaction, String> {

    List<Transaction> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
}
