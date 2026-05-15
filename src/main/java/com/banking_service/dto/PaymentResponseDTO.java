package com.banking_service.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponseDTO {

    private String referenceNumber;
    private String paymentStatus;
    private String message;
    private String paymentId;
    private String utrNumber;
    private String failureReason;
}
