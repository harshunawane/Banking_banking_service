package com.banking_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddBeneficiaryResponseDTO {

    private String message;

    private String beneficiaryStatus;

    private LocalDateTime activationTime;
}

