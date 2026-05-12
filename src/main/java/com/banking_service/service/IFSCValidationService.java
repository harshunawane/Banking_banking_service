package com.banking_service.service;

import com.banking_service.dto.IFSCValidationResponseDTO;
import com.banking_service.exception.BeneficiaryException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

@Service
public class IFSCValidationService {

    @Autowired
    private RestTemplate restTemplate;

    private static final String EXTERNAL_IFSC_VALIDATION_API = "http://localhost:8080/api/validateifsc/";

    /**
     * Validate IFSC code using external bank validation service
     * @param ifscCode - IFSC code to validate
     * @return IFSCValidationResponseDTO with bank details
     * @throws BeneficiaryException if IFSC validation fails
     */
    public IFSCValidationResponseDTO validateIFSC(String ifscCode) {
        try {
            String url = EXTERNAL_IFSC_VALIDATION_API + ifscCode;
            IFSCValidationResponseDTO response = restTemplate.getForObject(
                    url,
                    IFSCValidationResponseDTO.class
            );

            // Check if response is null
            if (response == null) {
                throw new BeneficiaryException("IFSC validation service returned null response for: " + ifscCode);
            }

            // Check if IFSC is invalid based on status field
            if ("Invalid".equals(response.getStatus())) {
                String errorMessage = response.getMessage() != null ? response.getMessage() : "Invalid IFSC code";
                throw new BeneficiaryException(errorMessage + ": " + ifscCode);
            }


            return response;

        } catch (RestClientException e) {
            // Handle HTTP errors (like 400 Bad Request)
            String errorMessage = e.getMessage();
            if (errorMessage.contains("400")) {
                throw new BeneficiaryException("Invalid IFSC code: " + ifscCode);
            } else if (errorMessage.contains("404")) {
                throw new BeneficiaryException("IFSC validation service not found. Please check if the service is running.");
            } else if (errorMessage.contains("Connection refused")) {
                throw new BeneficiaryException("Cannot connect to IFSC validation service. Please ensure the service is running on localhost:8080.");
            } else {
                throw new BeneficiaryException("External IFSC validation service error: " + errorMessage, e);
            }
        } catch (BeneficiaryException e) {
            // Re-throw our custom exceptions
            throw e;
        } catch (Exception e) {
            throw new BeneficiaryException("IFSC validation failed: " + e.getMessage(), e);
        }
    }
}
