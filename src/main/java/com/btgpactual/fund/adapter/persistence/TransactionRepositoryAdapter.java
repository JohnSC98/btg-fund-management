package com.btgpactual.fund.adapter.persistence;

import com.btgpactual.fund.adapter.persistence.mongo.MongoTransactionRepository;
import com.btgpactual.fund.domain.model.Transaction;
import com.btgpactual.fund.domain.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TransactionRepositoryAdapter implements TransactionRepository {

    private final MongoTransactionRepository mongoRepository;

    @Override
    public Transaction save(Transaction transaction) {
        return mongoRepository.save(transaction);
    }

    @Override
    public List<Transaction> findByUserIdOrderByCreatedAtDesc(String userId, int limit) {
        return mongoRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, limit));
    }
}
