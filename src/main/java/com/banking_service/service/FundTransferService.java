package com.banking_service.service;

import com.banking_service.dto.FundTransferRequestDTO;
import com.banking_service.dto.FundTransferResponseDTO;
import com.banking_service.dto.PaymentRequestDTO;
import com.banking_service.dto.PaymentResponseDTO;
import com.banking_service.entity.BeneficiaryDetails;
import com.banking_service.entity.CustomerAccount;
import com.banking_service.entity.TransactionDetails;
import com.banking_service.exception.BeneficiaryException;
import com.banking_service.repository.BeneficiaryRepository;
import com.banking_service.repository.CustomerAccountRepository;
import com.banking_service.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class FundTransferService {

    @Autowired
    private CustomerAccountRepository customerAccountRepository;

    @Autowired
    private BeneficiaryRepository beneficiaryRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PaymentService paymentService;

    // Constants for validation and charges
    private static final BigDecimal DAILY_LIMIT = new BigDecimal("5000.00"); // ₹50,000 daily limit
    private static final BigDecimal IMPS_CHARGE = new BigDecimal("5.00"); // ₹5 for IMPS
    private static final BigDecimal NEFT_CHARGE = new BigDecimal("2.00"); // ₹2 for NEFT
    private static final BigDecimal GST_RATE = new BigDecimal("0.18"); // 18% GST
    private static final int MAX_TRANSFERS_PER_MINUTE_PER_BENEFICIARY = 5;
    private static final int DUPLICATE_CHECK_MINUTES = 5;

    public FundTransferResponseDTO transferFund(FundTransferRequestDTO fundTransferRequestDTO) {

        String referenceNumber = generateReferenceNumber();

        try {

            // ==================================================
            // STEP 1 : VALIDATE AMOUNT
            // ==================================================

            validateAmountGreaterThanZero(
                    fundTransferRequestDTO.getTransferAmount());

            // ==================================================
            // STEP 2 : VALIDATE CUSTOMER ACCOUNT
            // ==================================================

            CustomerAccount customerAccount =
                    validateAndGetCustomerAccount(
                            fundTransferRequestDTO.getCustomerId());

            // ==================================================
            // STEP 3 : VALIDATE BENEFICIARY
            // ==================================================

            BeneficiaryDetails beneficiaryDetails =
                    validateAndGetBeneficiary(
                            fundTransferRequestDTO.getCustomerId(),
                            fundTransferRequestDTO.getBeneficiaryId());

            // ==================================================
            // STEP 4 : CHECK BENEFICIARY BLOCK STATUS
            // ==================================================

            validateBeneficiaryNotBlocked(beneficiaryDetails);

            // ==================================================
            // STEP 5 : CHECK DUPLICATE REFERENCE NUMBER
            // ==================================================

            validateDuplicateReferenceNumber(referenceNumber);

            // ==================================================
            // STEP 6 : CALCULATE CHARGES + GST
            // ==================================================

            BigDecimal charges =
                    calculateTransferCharges(
                            fundTransferRequestDTO.getTransferType());

            BigDecimal gst = calculateGST(charges);

            BigDecimal totalDebitAmount =
                    fundTransferRequestDTO.getTransferAmount()
                            .add(charges)
                            .add(gst);

            // ==================================================
            // STEP 7 : CHECK SUFFICIENT BALANCE
            // ==================================================

            validateSufficientBalance(
                    customerAccount,
                    totalDebitAmount);

            // ==================================================
            // STEP 8 : CHECK DAILY LIMIT
            // ==================================================

            validateDailyLimit(
                    fundTransferRequestDTO.getCustomerId(),
                    fundTransferRequestDTO.getTransferAmount());

            // ==================================================
            // STEP 9 : CHECK RATE LIMIT
            // ==================================================

            validateRateLimitPerBeneficiary(
                    fundTransferRequestDTO.getCustomerId(),
                    fundTransferRequestDTO.getBeneficiaryId());

            // ==================================================
            // STEP 10 : CREATE PENDING TRANSACTION
            // ==================================================

            TransactionDetails transaction =
                    createPendingTransaction(
                            fundTransferRequestDTO,
                            referenceNumber,
                            charges,
                            gst,
                            totalDebitAmount);

            // ==================================================
            // STEP 11 : DEBIT CUSTOMER BALANCE
            // ==================================================

            updateCustomerBalance(
                    customerAccount,
                    totalDebitAmount);

            // ==================================================
            // STEP 12 : CREATE PAYMENT REQUEST
            // ==================================================

            PaymentRequestDTO paymentRequest =
                    createPaymentRequest(
                            fundTransferRequestDTO,
                            transaction,
                            customerAccount,
                            beneficiaryDetails);

            // ==================================================
            // STEP 13 : CALL EXTERNAL PAYMENT SERVICE
            // ==================================================

            PaymentResponseDTO paymentResponse =
                    paymentService.processPayment(paymentRequest);

            // ==================================================
            // STEP 14 : PROCESS PAYMENT RESPONSE
            // ==================================================

            return processPaymentResponse(
                    transaction,
                    paymentResponse,
                    customerAccount,
                    totalDebitAmount);

        } catch (BeneficiaryException e) {

            TransactionDetails failedTransaction =
                    createFailedTransaction(
                            fundTransferRequestDTO,
                            referenceNumber,
                            e.getMessage());

            return createFailureResponse(
                    failedTransaction,
                    e.getMessage());

        } catch (Exception e) {

            TransactionDetails failedTransaction =
                    createFailedTransaction(
                            fundTransferRequestDTO,
                            referenceNumber,
                            "Unexpected system error");

            return createFailureResponse(
                    failedTransaction,
                    "Fund transfer failed due to system error");
        }
    }

    // Validation Methods

    private void validateAmountGreaterThanZero(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BeneficiaryException("Transfer amount must be greater than zero");
        }
    }

    private CustomerAccount validateAndGetCustomerAccount(Integer customerId) {
        CustomerAccount customerAccount = customerAccountRepository.findById(customerId)
                .orElseThrow(() -> new BeneficiaryException("Customer account not found with ID: " + customerId));

        if (!"ACTIVE".equals(customerAccount.getAccountStatus())) {
            throw new BeneficiaryException("Customer account is not ACTIVE. Current status: " + customerAccount.getAccountStatus());
        }

        return customerAccount;
    }

    private BeneficiaryDetails validateAndGetBeneficiary(Integer customerId, Integer beneficiaryId) {
        BeneficiaryDetails beneficiary = beneficiaryRepository.findById(beneficiaryId)
                .orElseThrow(() -> new BeneficiaryException("Beneficiary not found with ID: " + beneficiaryId));
System.out.println("***************** Benificiary acc no : " +beneficiary.getBeneficiaryAccountNumber() );
        // Check if beneficiary belongs to the customer
        if (!beneficiary.getCustomerId().equals(customerId)) {
            throw new BeneficiaryException("Beneficiary does not belong to the specified customer");
        }

        // Check if beneficiary is ACTIVE
        if (!"ACTIVE".equals(beneficiary.getBeneficiaryStatus())) {
            throw new BeneficiaryException("Beneficiary is not ACTIVE. Current status: " + beneficiary.getBeneficiaryStatus());
        }

        return beneficiary;
    }

    private void validateBeneficiaryNotBlocked(BeneficiaryDetails beneficiary) {
        if ("BLOCKED".equals(beneficiary.getBeneficiaryStatus())) {
            throw new BeneficiaryException("Beneficiary account is BLOCKED");
        }
    }

    private void validateDuplicateReferenceNumber(String referenceNumber) {
        Optional<TransactionDetails> existingTransaction = transactionRepository.findByReferenceNumber(referenceNumber);
        if (existingTransaction.isPresent()) {
            throw new BeneficiaryException("Duplicate transaction request detected. Reference number already exists");
        }
    }

    private void validateSufficientBalance(CustomerAccount customerAccount, BigDecimal totalDebitAmount) {
        System.out.println("*************************** Step 1: ");
        if (customerAccount.getAvailableBalance() == null ||
                customerAccount.getAvailableBalance().compareTo(totalDebitAmount) < 0) {
            System.out.println( "******** Avaible balance from Customer table" +customerAccount.getAvailableBalance());
            throw new BeneficiaryException("Insufficient balance. Available: ₹" + customerAccount.getAvailableBalance() + ", Required: ₹" + totalDebitAmount);
        }
    }

    private void validateDailyLimit(Integer customerId, BigDecimal transferAmount) {
        System.out.println("*************************** Step 2: ");
        LocalDateTime today = LocalDateTime.now();
        BigDecimal totalTodayAmount = transactionRepository.getTotalTransactionAmountByCustomerAndDate(customerId.longValue(), today);

        if (totalTodayAmount.add(transferAmount).compareTo(DAILY_LIMIT) > 0) {
            throw new BeneficiaryException("Daily transfer limit exceeded. Current daily total: ₹" + totalTodayAmount + ", Limit: ₹" + DAILY_LIMIT);
        }
    }



    private void validateRateLimitPerBeneficiary(Integer customerId, Integer beneficiaryId) {
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
        long recentTransactionsCount = transactionRepository.countRecentTransactionsByBeneficiary(
                customerId.longValue(), beneficiaryId.longValue(), oneMinuteAgo);

        if (recentTransactionsCount >= MAX_TRANSFERS_PER_MINUTE_PER_BENEFICIARY) {
            throw new BeneficiaryException("Rate limit exceeded. Maximum " + MAX_TRANSFERS_PER_MINUTE_PER_BENEFICIARY + " transfers allowed per minute per beneficiary");
        }
    }

    // Utility Methods

    private String generateReferenceNumber() {
        return "FT" + UUID.randomUUID().toString().substring(0, 8).toUpperCase() + System.currentTimeMillis();
    }

    private BigDecimal calculateTransferCharges(String transferType) {
        if ("IMPS".equalsIgnoreCase(transferType)) {
            return IMPS_CHARGE;
        } else if ("NEFT".equalsIgnoreCase(transferType)) {
            return NEFT_CHARGE;
        } else {
            throw new BeneficiaryException("Invalid transfer type. Supported types: IMPS, NEFT");
        }
    }

    private BigDecimal calculateGST(BigDecimal charges) {
        return charges.multiply(GST_RATE);
    }

    private TransactionDetails createPendingTransaction(FundTransferRequestDTO request, String referenceNumber,
                                                      BigDecimal charges, BigDecimal gst, BigDecimal totalDebitAmount) {
        TransactionDetails transaction = new TransactionDetails();
        transaction.setReferenceNumber(referenceNumber);
        transaction.setCustomerId(request.getCustomerId());
        transaction.setBeneficiaryId(request.getBeneficiaryId());
        transaction.setAmount(request.getTransferAmount());
        transaction.setCharges(charges);
        transaction.setGst(gst);
        transaction.setTotalDebitAmount(totalDebitAmount);
        transaction.setTransactionStatus("PENDING");
        transaction.setRemarks(request.getRemarks());
        transaction.setTransferType(request.getTransferType());
        transaction.setFailureReason(null);
        transaction.setCreatedTime(LocalDateTime.now());

        return transactionRepository.save(transaction);
    }

    private TransactionDetails createFailedTransaction(FundTransferRequestDTO request, String referenceNumber, String failureReason) {
        TransactionDetails transaction = new TransactionDetails();
        transaction.setReferenceNumber(referenceNumber);
        transaction.setCustomerId(request.getCustomerId());
        transaction.setBeneficiaryId(request.getBeneficiaryId());
        transaction.setAmount(request.getTransferAmount() != null ? request.getTransferAmount() : BigDecimal.ZERO);
        transaction.setCharges(BigDecimal.ZERO);
        transaction.setGst(BigDecimal.ZERO);
        transaction.setTotalDebitAmount(BigDecimal.ZERO);
        transaction.setTransactionStatus("FAILED");
        transaction.setRemarks(request.getRemarks());
        transaction.setTransferType(request.getTransferType());
        transaction.setFailureReason(failureReason);
        transaction.setCreatedTime(LocalDateTime.now());

        return transactionRepository.save(transaction);
    }

    private void updateCustomerBalance(CustomerAccount customerAccount, BigDecimal debitAmount) {
        BigDecimal newBalance = customerAccount.getAvailableBalance().subtract(debitAmount);
        customerAccount.setAvailableBalance(newBalance);
        customerAccountRepository.save(customerAccount);
    }

    private void revertCustomerBalance(CustomerAccount customerAccount, BigDecimal creditAmount) {
        BigDecimal newBalance = customerAccount.getAvailableBalance().add(creditAmount);
        customerAccount.setAvailableBalance(newBalance);
        customerAccountRepository.save(customerAccount);
    }

    private PaymentRequestDTO createPaymentRequest(FundTransferRequestDTO fundTransferRequest,
                                                 TransactionDetails transaction,
                                                 CustomerAccount customerAccount,
                                                 BeneficiaryDetails beneficiary) {
        PaymentRequestDTO paymentRequest = new PaymentRequestDTO();
        paymentRequest.setReferenceNumber(transaction.getReferenceNumber());
        paymentRequest.setCustomerId(fundTransferRequest.getCustomerId());
        paymentRequest.setBeneficiaryId(fundTransferRequest.getBeneficiaryId());
        paymentRequest.setFromAccountNumber(customerAccount.getAccountNumber());
        paymentRequest.setToAccountNumber(beneficiary.getBeneficiaryAccountNumber());
        paymentRequest.setAmount(transaction.getAmount());
        paymentRequest.setCharges(transaction.getCharges());
        paymentRequest.setGst(transaction.getGst());
        paymentRequest.setTotalDebitAmount(transaction.getTotalDebitAmount());
        paymentRequest.setTransferType(transaction.getTransferType());
        paymentRequest.setRemarks(transaction.getRemarks());

        return paymentRequest;
    }

    private FundTransferResponseDTO processPaymentResponse(TransactionDetails transaction,
                                                         PaymentResponseDTO paymentResponse,
                                                         CustomerAccount customerAccount,
                                                         BigDecimal totalDebitAmount) {

        if ("SUCCESS".equals(paymentResponse.getPaymentStatus())) {
            // Payment successful - update transaction to SUCCESS
            transaction.setTransactionStatus("SUCCESS");
            transaction.setFailureReason(null);
            TransactionDetails savedTransaction = transactionRepository.save(transaction);

            return createSuccessResponse(savedTransaction, customerAccount);

        } else {
            // Payment failed - revert balance and update transaction to FAILED
            revertCustomerBalance(customerAccount, totalDebitAmount);
            transaction.setTransactionStatus("FAILED");
            transaction.setFailureReason(paymentResponse.getFailureReason() != null ?
                    paymentResponse.getFailureReason() : paymentResponse.getMessage());
            TransactionDetails savedTransaction = transactionRepository.save(transaction);

            return createFailureResponse(savedTransaction,
                    paymentResponse.getMessage() != null ? paymentResponse.getMessage() : "Payment processing failed");
        }
    }

    private FundTransferResponseDTO createSuccessResponse(TransactionDetails transaction, CustomerAccount customerAccount) {
        // Get beneficiary details for toAccountNumber
        BeneficiaryDetails beneficiary = beneficiaryRepository.findById(transaction.getBeneficiaryId().intValue())
                .orElseThrow(() -> new BeneficiaryException("Beneficiary not found"));

        FundTransferResponseDTO response = new FundTransferResponseDTO();
        response.setReferenceNumber(transaction.getReferenceNumber());
        response.setTransactionId(transaction.getTransactionId());
        response.setTransactionStatus(transaction.getTransactionStatus());
        response.setMessage("Fund transfer completed successfully");
        response.setCustomerId(transaction.getCustomerId().intValue());
        response.setBeneficiaryId(transaction.getBeneficiaryId().intValue());
        response.setFromAccountNumber(customerAccount.getAccountNumber());
        response.setToAccountNumber(beneficiary.getBeneficiaryAccountNumber());
        response.setAmount(transaction.getAmount());
        response.setCharges(transaction.getCharges());
        response.setGst(transaction.getGst());
        response.setTotalDebitAmount(transaction.getTotalDebitAmount());
        response.setTransferType(transaction.getTransferType());
        response.setRemarks(transaction.getRemarks());
        response.setTransactionTime(transaction.getCreatedTime());

        return response;
    }

    private FundTransferResponseDTO createFailureResponse(TransactionDetails transaction, String errorMessage) {
        // Fetch customer and beneficiary details for complete response
        CustomerAccount customerAccount = customerAccountRepository.findById(transaction.getCustomerId().intValue())
                .orElse(null);
        BeneficiaryDetails beneficiary = beneficiaryRepository.findById(transaction.getBeneficiaryId().intValue())
                .orElse(null);

        FundTransferResponseDTO response = new FundTransferResponseDTO();
        response.setReferenceNumber(transaction.getReferenceNumber());
        response.setTransactionId(transaction.getTransactionId());
        response.setTransactionStatus(transaction.getTransactionStatus());
        response.setMessage(errorMessage);
        response.setCustomerId(transaction.getCustomerId().intValue());
        response.setBeneficiaryId(transaction.getBeneficiaryId().intValue());
      //  System.out.println("Create ")
    response.setFromAccountNumber(customerAccount != null ? customerAccount.getAccountNumber() : null);
        response.setToAccountNumber(beneficiary != null ? beneficiary.getBeneficiaryAccountNumber() : null);
        response.setAmount(transaction.getAmount());
        response.setCharges(transaction.getCharges());
        response.setGst(transaction.getGst());
        response.setTotalDebitAmount(transaction.getTotalDebitAmount());
        response.setTransferType(transaction.getTransferType());
        response.setRemarks(transaction.getRemarks());
        response.setTransactionTime(transaction.getCreatedTime());

        return response;
    }

    // Additional API methods for transaction history

    public TransactionDetails getTransactionByReferenceNumber(String referenceNumber) {
        return transactionRepository.findByReferenceNumber(referenceNumber)
                .orElseThrow(() -> new BeneficiaryException("Transaction not found with reference number: " + referenceNumber));
    }

    public java.util.List<TransactionDetails> getTransactionHistory(Long customerId) {
        return transactionRepository.findByCustomerIdOrderByCreatedTimeDesc(customerId);
    }

    private FundTransferResponseDTO createFundTransferResponseDTO(FundTransferRequestDTO fundTransferRequestDTO, String referenceNumber, CustomerAccount customerAccount, BeneficiaryDetails beneficiaryDetails, BigDecimal charges, BigDecimal gst, BigDecimal totalDebitAmount) {
        FundTransferResponseDTO responseDTO = new FundTransferResponseDTO();
        responseDTO.setReferenceNumber(referenceNumber);
        responseDTO.setCustomerId(fundTransferRequestDTO.getCustomerId());
        responseDTO.setBeneficiaryId(fundTransferRequestDTO.getBeneficiaryId());
        System.out.println("*************" +   fundTransferRequestDTO.getBeneficiaryId());
        responseDTO.setFromAccountNumber(customerAccount.getAccountNumber());
        System.out.println("*************" +   customerAccount.getAccountNumber());
        responseDTO.setToAccountNumber(beneficiaryDetails.getBeneficiaryAccountNumber());
        System.out.println("*************" +   beneficiaryDetails.getBeneficiaryAccountNumber());
        responseDTO.setAmount(fundTransferRequestDTO.getTransferAmount());
        responseDTO.setCharges(charges);
        responseDTO.setGst(gst);
        responseDTO.setTotalDebitAmount(totalDebitAmount);
        responseDTO.setTransferType(fundTransferRequestDTO.getTransferType());
        responseDTO.setRemarks(fundTransferRequestDTO.getRemarks());
        responseDTO.setTransactionStatus("PENDING");
        responseDTO.setMessage("Fund transfer initiated, awaiting payment processing");
        responseDTO.setTransactionTime(LocalDateTime.now());

        return responseDTO;
    }

    private PaymentRequestDTO mapToPaymentRequest(FundTransferResponseDTO responseDTO) {
        PaymentRequestDTO paymentRequest = new PaymentRequestDTO();
        paymentRequest.setReferenceNumber(responseDTO.getReferenceNumber());
        paymentRequest.setCustomerId(responseDTO.getCustomerId());
        paymentRequest.setTransactionId(responseDTO.getTransactionId());
        System.out.println( "***** transaction Id send to paymentRequest" + responseDTO.getTransactionId());
        paymentRequest.setBeneficiaryId(responseDTO.getBeneficiaryId());
        paymentRequest.setFromAccountNumber(responseDTO.getFromAccountNumber());
        paymentRequest.setToAccountNumber(responseDTO.getToAccountNumber());
        paymentRequest.setAmount(responseDTO.getAmount());
        paymentRequest.setCharges(responseDTO.getCharges());
        paymentRequest.setGst(responseDTO.getGst());
        paymentRequest.setTotalDebitAmount(responseDTO.getTotalDebitAmount());
        paymentRequest.setTransferType(responseDTO.getTransferType());
        paymentRequest.setRemarks(responseDTO.getRemarks());

        return paymentRequest;
    }

    private FundTransferResponseDTO processPaymentAndCreateTransaction(FundTransferResponseDTO responseDTO, PaymentResponseDTO paymentResponse, CustomerAccount customerAccount, BigDecimal totalDebitAmount) {
        TransactionDetails transaction = new TransactionDetails();
        transaction.setReferenceNumber(responseDTO.getReferenceNumber());
        transaction.setCustomerId(responseDTO.getCustomerId());
        transaction.setBeneficiaryId(responseDTO.getBeneficiaryId());
        transaction.setAmount(responseDTO.getAmount());
        transaction.setCharges(responseDTO.getCharges());
        transaction.setGst(responseDTO.getGst());
        transaction.setTotalDebitAmount(responseDTO.getTotalDebitAmount());
        transaction.setTransactionStatus(paymentResponse.getPaymentStatus());
        transaction.setRemarks(responseDTO.getRemarks());
        transaction.setTransferType(responseDTO.getTransferType());
        transaction.setFailureReason(paymentResponse.getFailureReason() != null ?
                paymentResponse.getFailureReason() : paymentResponse.getMessage());
        transaction.setCreatedTime(LocalDateTime.now());

        // Save transaction record
        TransactionDetails savedTransaction = transactionRepository.save(transaction);

        if ("SUCCESS".equals(paymentResponse.getPaymentStatus())) {
            // Payment successful - update customer balance
            updateCustomerBalance(customerAccount, totalDebitAmount);

            return createSuccessResponse(savedTransaction, customerAccount);
        } else {
            // Payment failed - revert any temporary changes
            revertCustomerBalance(customerAccount, totalDebitAmount);

            return createFailureResponse(savedTransaction,
                    paymentResponse.getMessage() != null ? paymentResponse.getMessage() : "Payment processing failed");
        }
    }
}
