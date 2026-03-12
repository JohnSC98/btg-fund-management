package com.btgpactual.fund.domain.repository;

import com.btgpactual.fund.domain.model.Fund;

import java.util.List;
import java.util.Optional;

public interface FundRepository {

    Optional<Fund> findById(String id);

    Optional<Fund> findByCode(String code);

    List<Fund> findAll();

    Fund save(Fund fund);
}
