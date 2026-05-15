package com.banking_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayementRequestDTO {

    private Integer customerId;       // will get from customer table
    private Integer transactionId;    // will get this from transaction table
   private  String fromAccountNumber; // Customer Acc number
    private String toAccountNumber; // beneficiary acc number
    private BigDecimal transferAmount;
    private String referenceNumber;   // generate when validation success and send to payment
    private String transferType;     // send from request

    private String remarks;

}
