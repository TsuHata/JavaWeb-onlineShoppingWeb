package com.example.auth.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class RefundRequest {
    private Long paymentId;
    private BigDecimal amount;
    private String reason;

}