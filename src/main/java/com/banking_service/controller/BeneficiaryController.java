package com.banking_service.controller;

import com.banking_service.dto.AddBeneficiaryRequestDTO;
import com.banking_service.dto.AddBeneficiaryResponseDTO;
import com.banking_service.entity.BeneficiaryDetails;
import com.banking_service.exception.BeneficiaryException;
import com.banking_service.service.BeneficiaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/beneficiary")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BeneficiaryController {

    @Autowired
    private BeneficiaryService beneficiaryService;

    /**
     * Add a new beneficiary
     */
    @PostMapping("/add")
    public ResponseEntity<AddBeneficiaryResponseDTO> addBeneficiary(@RequestBody AddBeneficiaryRequestDTO requestDTO) throws BeneficiaryException {

        AddBeneficiaryResponseDTO response = beneficiaryService.addBeneficiary(requestDTO);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    /**
     * Get all active beneficiaries for a customer
     */
    @GetMapping("/active/{customerId}")
    public ResponseEntity<List<BeneficiaryDetails>> getActiveBeneficiaries(@PathVariable Integer customerId) throws BeneficiaryException {
        List<BeneficiaryDetails> beneficiaries = beneficiaryService.getActiveBeneficiaries(customerId);
        return new ResponseEntity<>(beneficiaries, HttpStatus.OK);
    }
}