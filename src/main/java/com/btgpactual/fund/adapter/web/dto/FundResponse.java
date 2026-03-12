package com.btgpactual.fund.adapter.web.dto;

import com.btgpactual.fund.domain.model.Fund;

import java.math.BigDecimal;

public record FundResponse(
        String id,
        String code,
        String name,
        BigDecimal minAmount,
        String category
) {
    public static FundResponse from(Fund fund) {
        return new FundResponse(
                fund.getId(),
                fund.getCode(),
                fund.getName(),
                fund.getMinAmount(),
                fund.getCategory().name()
        );
    }
}
