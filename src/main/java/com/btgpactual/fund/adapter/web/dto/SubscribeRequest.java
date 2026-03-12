package com.btgpactual.fund.adapter.web.dto;

import jakarta.validation.constraints.NotBlank;

public record SubscribeRequest(
        @NotBlank(message = "fundCode es obligatorio") String fundCode
) {}
