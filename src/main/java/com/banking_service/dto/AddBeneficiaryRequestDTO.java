package com.banking_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddBeneficiaryRequestDTO {

    private Integer customerId;

    private String beneficiaryName;

    private String beneficiaryAccountNumber;

    private String confirmAccountNumber;

    private String ifscCode;

    private String nickname;
}

