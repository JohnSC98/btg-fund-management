package com.btgpactual.fund.domain.repository;

import com.btgpactual.fund.domain.model.Transaction;

import java.util.List;

public interface TransactionRepository {

    Transaction save(Transaction transaction);

    List<Transaction> findByUserIdOrderByCreatedAtDesc(String userId, int limit);
}
