package com.banking_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FundTransferResponseDTO {

    private String referenceNumber;
    private Integer transactionId;
    private String transactionStatus;
    private String message;
    private Integer customerId;
    private Integer beneficiaryId;
    private String fromAccountNumber;
    private String toAccountNumber;
    private BigDecimal amount;
    private BigDecimal charges;
    private BigDecimal gst;
    private BigDecimal totalDebitAmount;
    private String transferType;
    private String remarks;
    private LocalDateTime transactionTime;
}
