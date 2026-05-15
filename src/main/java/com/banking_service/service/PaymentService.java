package com.banking_service.service;

import com.banking_service.dto.PaymentRequestDTO;
import com.banking_service.dto.PaymentResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class PaymentService {

    @Autowired
    private RestTemplate restTemplate;

    private static final String PAYMENT_API_URL = "http://localhost:8082/payment/process";

    /**
     * Process payment through external payment service
     * @param paymentRequest Payment request details
     * @return Payment response from external service
     * @throws RestClientException if payment service is unavailable
     */
    public PaymentResponseDTO processPayment(PaymentRequestDTO paymentRequest) throws RestClientException {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");

            HttpEntity<PaymentRequestDTO> entity = new HttpEntity<>(paymentRequest, headers);

            ResponseEntity<PaymentResponseDTO> response = restTemplate.exchange(
                    PAYMENT_API_URL,
                    HttpMethod.POST,
                    entity,
                    PaymentResponseDTO.class
            );

            return response.getBody();

        } catch (RestClientException e) {
            // Create failure response for external service errors
            PaymentResponseDTO failureResponse = new PaymentResponseDTO();
            failureResponse.setReferenceNumber(paymentRequest.getReferenceNumber());
            failureResponse.setPaymentStatus("FAILED");
            failureResponse.setMessage("Payment service unavailable");
            failureResponse.setFailureReason("External payment service error: " + e.getMessage());
            return failureResponse;
        }
    }
}
