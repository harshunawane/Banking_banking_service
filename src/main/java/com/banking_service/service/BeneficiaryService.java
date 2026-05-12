package com.banking_service.service;

import com.banking_service.dto.AddBeneficiaryRequestDTO;
import com.banking_service.dto.AddBeneficiaryResponseDTO;
import com.banking_service.dto.IFSCValidationResponseDTO;
import com.banking_service.entity.BeneficiaryDetails;
import com.banking_service.entity.CustomerAccount;
import com.banking_service.exception.BeneficiaryException;
import com.banking_service.repository.BeneficiaryRepository;
import com.banking_service.repository.CustomerAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BeneficiaryService {

    @Autowired
    private BeneficiaryRepository beneficiaryRepository;

    @Autowired
    private CustomerAccountRepository customerAccountRepository;

    @Autowired
    private IFSCValidationService ifscValidationService;

    /**
     * Add a new beneficiary with comprehensive validations
     */
    public AddBeneficiaryResponseDTO addBeneficiary(AddBeneficiaryRequestDTO requestDTO) throws     BeneficiaryException {

        // Validation 1: Account numbers must match
        validateAccountNumbersMatch(requestDTO);

        // Validation 2: Fetch and validate customer account
        CustomerAccount customerAccount = validateAndGetCustomerAccount(requestDTO.getCustomerId());

        // Validation 3: Check if customer account is BLOCKED or FROZEN
        validateAccountStatusNotBlockedOrFrozen(customerAccount);

        // Validation 4: Validate IFSC code with external service and get bank details
        IFSCValidationResponseDTO ifscValidation = ifscValidationService.validateIFSC(requestDTO.getIfscCode());

        // Validation 5: Check if beneficiary account belongs to logged-in customer
        validateBeneficiaryAccountNotOwnAccount(requestDTO);

        // Validation 6: Check for duplicate beneficiary
        Optional<BeneficiaryDetails> existingBeneficiary = beneficiaryRepository.findDuplicateBeneficiary(
                requestDTO.getCustomerId(),
                requestDTO.getBeneficiaryAccountNumber(),
                requestDTO.getIfscCode()
        );

        // If inactive beneficiary exists, reactivate it
        if (existingBeneficiary.isPresent()) {
            BeneficiaryDetails beneficiary = existingBeneficiary.get();
            if ("INACTIVE".equals(beneficiary.getBeneficiaryStatus())) {
                return reactivateBeneficiary(beneficiary);
            } else {
                throw new BeneficiaryException("Duplicate beneficiary already exists for this customer");
            }
        }

        // Validation 7: Check if customer already has 10 active beneficiaries
        long activeBeneficiaryCount = beneficiaryRepository.countActiveBeneficiaries(requestDTO.getCustomerId());
        if (activeBeneficiaryCount >= 10) {
            throw new BeneficiaryException("Customer cannot have more than 10 active beneficiaries");
        }

        // Create new beneficiary with PENDING_ACTIVATION status
        BeneficiaryDetails beneficiary = new BeneficiaryDetails();
        beneficiary.setCustomerId(requestDTO.getCustomerId());
        beneficiary.setBeneficiaryName(requestDTO.getBeneficiaryName());
        beneficiary.setBeneficiaryAccountNumber(requestDTO.getBeneficiaryAccountNumber());
        beneficiary.setIfscCode(requestDTO.getIfscCode());
        beneficiary.setNickname(requestDTO.getNickname());
        beneficiary.setBankName(ifscValidation.getBankName());
        beneficiary.setBranchName(ifscValidation.getBranchName());
        beneficiary.setBeneficiaryStatus("PENDING_ACTIVATION");
        beneficiary.setCreatedDate(LocalDateTime.now());

        BeneficiaryDetails savedBeneficiary = beneficiaryRepository.save(beneficiary);

        // Return response
        AddBeneficiaryResponseDTO responseDTO = new AddBeneficiaryResponseDTO();
        responseDTO.setMessage("Beneficiary added successfully. Will be activated in 1 minute.");
        responseDTO.setBeneficiaryStatus(savedBeneficiary.getBeneficiaryStatus());
        responseDTO.setActivationTime(null); // Will be set after 1 minute

        return responseDTO;
    }

    /**
     * Reactivate an existing inactive beneficiary
     */
    private AddBeneficiaryResponseDTO reactivateBeneficiary(BeneficiaryDetails beneficiary) {
        beneficiary.setBeneficiaryStatus("PENDING_ACTIVATION");
        beneficiary.setActivationTime(null);
        BeneficiaryDetails updatedBeneficiary = beneficiaryRepository.save(beneficiary);

        AddBeneficiaryResponseDTO responseDTO = new AddBeneficiaryResponseDTO();
        responseDTO.setMessage("Existing beneficiary reactivated successfully. Will be activated in 1 minute.");
        responseDTO.setBeneficiaryStatus(updatedBeneficiary.getBeneficiaryStatus());
        responseDTO.setActivationTime(null);

        return responseDTO;
    }

    /**
     * Scheduled task to activate beneficiaries after 1 minute
     * Runs every minute to check and activate pending beneficiaries
     */
    @Scheduled(fixedRate = 60000) // Run every 60 seconds
    public void activatePendingBeneficiaries() {
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);

        // Find all PENDING_ACTIVATION beneficiaries created more than 1 minute ago
        List<BeneficiaryDetails> allBeneficiaries = beneficiaryRepository.findAll();

        for (BeneficiaryDetails beneficiary : allBeneficiaries) {
            if ("PENDING_ACTIVATION".equals(beneficiary.getBeneficiaryStatus())
                    && beneficiary.getCreatedDate() != null
                    && beneficiary.getCreatedDate().isBefore(oneMinuteAgo)) {

                beneficiary.setBeneficiaryStatus("ACTIVE");
                beneficiary.setActivationTime(LocalDateTime.now());
                beneficiaryRepository.save(beneficiary);
            }
        }
    }

    /**
     * Get all active beneficiaries for a customer
     */
    public List<BeneficiaryDetails> getActiveBeneficiaries(Integer customerId) {
        // Validate customer exists
        validateAndGetCustomerAccount(customerId);

        return beneficiaryRepository.findAll().stream()
                .filter(b -> b.getCustomerId().equals(customerId) && "ACTIVE".equals(b.getBeneficiaryStatus()))
                .toList();
    }

    /**
     * Validation: Account numbers must match
     */
    private void validateAccountNumbersMatch(AddBeneficiaryRequestDTO requestDTO) {
        if (!requestDTO.getBeneficiaryAccountNumber().equals(requestDTO.getConfirmAccountNumber())) {
            throw new BeneficiaryException("Beneficiary account number and confirm account number must match");
        }
    }

    /**
     * Validation: Get and validate customer account exists
     */
    private CustomerAccount validateAndGetCustomerAccount(Integer customerId) {
        return customerAccountRepository.findById(customerId)
                .orElseThrow(() -> new BeneficiaryException("Customer account not found with ID: " + customerId));
    }

    /**
     * Validation: Check account status is not BLOCKED or FROZEN
     */
    private void validateAccountStatusNotBlockedOrFrozen(CustomerAccount customerAccount) {
        String status = customerAccount.getAccountStatus();
        if ("BLOCKED".equals(status) || "FROZEN".equals(status)) {
            throw new BeneficiaryException("Beneficiary addition not allowed for " + status + " customer account");
        }
    }

    /**
     * Validation: Beneficiary account should not belong to logged-in customer
     */
    private void validateBeneficiaryAccountNotOwnAccount(AddBeneficiaryRequestDTO requestDTO) {
        Optional<CustomerAccount> customerAccount = customerAccountRepository
                .findByAccountNumber(requestDTO.getBeneficiaryAccountNumber());

        if (customerAccount.isPresent()) {
            if (customerAccount.get().getCustomerId().equals(requestDTO.getCustomerId())) {
                throw new BeneficiaryException("Beneficiary account cannot be the same as customer's own account");
            }
        }
    }
}

