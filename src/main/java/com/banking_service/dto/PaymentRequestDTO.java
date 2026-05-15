package com.banking_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDTO {

    private String referenceNumber;
    private Integer customerId;
    private Integer transactionId;
    private Integer beneficiaryId;
    private String fromAccountNumber;
    private String toAccountNumber;
    private BigDecimal amount;
    private BigDecimal charges;
    private BigDecimal gst;
    private BigDecimal totalDebitAmount;
    private String transferType;
    private String remarks;
}
