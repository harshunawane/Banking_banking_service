package com.banking_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IFSCValidationResponseDTO {

    private String status;        // "Valid" or "Invalid"
    private String bankName;
    private String branchName;
    private String ifscCode;
    private String message;       // Error message for invalid IFSC
    private Integer errorCode;    // Error code (400 for invalid)
    private String timestamp;     // Timestamp of the response
}
