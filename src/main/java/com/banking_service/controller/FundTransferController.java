package com.banking_service.controller;

import com.banking_service.dto.FundTransferRequestDTO;
import com.banking_service.dto.FundTransferResponseDTO;
import com.banking_service.entity.TransactionDetails;
import com.banking_service.exception.BeneficiaryException;
import com.banking_service.service.FundTransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/fund-transfer")
@CrossOrigin(origins = "*", maxAge = 3600)
public class FundTransferController {

    @Autowired
    private FundTransferService fundTransferService;

    /**
     * Transfer funds to a beneficiary
     */
    @PostMapping("/transfer")
    public ResponseEntity<?> transferFund(@RequestBody FundTransferRequestDTO requestDTO) {
        try {
            FundTransferResponseDTO response = fundTransferService.transferFund(requestDTO);
            return ResponseEntity.ok(response);
        } catch (BeneficiaryException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get transaction details by reference number
     */
    @GetMapping("/;/{referenceNumber}")
    public ResponseEntity<?> getTransactionByReferenceNumber(@PathVariable String referenceNumber) {
        try {
            TransactionDetails transaction = fundTransferService.getTransactionByReferenceNumber(referenceNumber);
            return ResponseEntity.ok(transaction);
        } catch (BeneficiaryException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get transaction history for a customer
     */
    @GetMapping("/history/{customerId}")
    public ResponseEntity<?> getTransactionHistory(@PathVariable Long customerId) {
        try {
            List<TransactionDetails> transactions = fundTransferService.getTransactionHistory(customerId);
            return ResponseEntity.ok(transactions);
        } catch (BeneficiaryException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
