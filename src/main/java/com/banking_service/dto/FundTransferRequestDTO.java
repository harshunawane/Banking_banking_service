package com.banking_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FundTransferRequestDTO {

    private Integer customerId;

    private Integer beneficiaryId;

    private BigDecimal transferAmount;

    private String transferType;

    private String remarks;
}
