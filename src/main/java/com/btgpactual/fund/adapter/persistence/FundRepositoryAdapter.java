package com.btgpactual.fund.adapter.persistence;

import com.btgpactual.fund.adapter.persistence.mongo.MongoFundRepository;
import com.btgpactual.fund.domain.model.Fund;
import com.btgpactual.fund.domain.repository.FundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FundRepositoryAdapter implements FundRepository {

    private final MongoFundRepository mongoRepository;

    @Override
    public Optional<Fund> findById(String id) {
        return mongoRepository.findById(id);
    }

    @Override
    public Optional<Fund> findByCode(String code) {
        return mongoRepository.findByCode(code);
    }

    @Override
    public List<Fund> findAll() {
        return mongoRepository.findAll();
    }

    @Override
    public Fund save(Fund fund) {
        return mongoRepository.save(fund);
    }
}
