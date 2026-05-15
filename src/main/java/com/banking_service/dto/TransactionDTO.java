package com.banking_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {

    private Long transactionId;

    private String referenceNumber;

    private Long customerId;

    private Long beneficiaryId;

    private BigDecimal amount;

    private BigDecimal charges;

    private BigDecimal gst;

    private BigDecimal totalDebitAmount;

    private String transactionStatus;

    private String remarks;

    private String transferType;

    private String failureReason;

    private LocalDateTime createdTime;
}
