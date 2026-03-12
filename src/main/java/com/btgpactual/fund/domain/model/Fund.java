package com.btgpactual.fund.domain.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Data
@Builder
@Document(collection = "funds")
public class Fund {

    @Id
    private String id;

    @Indexed(unique = true)
    private String code;

    private String name;

    /**
     * Minimum amount (COP) required to subscribe.
     */
    private BigDecimal minAmount;

    private FundCategory category;

    public enum FundCategory {
        FPV,
        FIC
    }
}
