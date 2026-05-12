package com.banking_service.repository;

import com.banking_service.entity.BeneficiaryDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BeneficiaryRepository extends JpaRepository<BeneficiaryDetails, Integer> {

    /**
     * Find duplicate beneficiary by account number, IFSC code and customer ID
     */
    @Query("SELECT b FROM BeneficiaryDetails b WHERE b.customerId = :customerId " +
            "AND b.beneficiaryAccountNumber = :accountNumber AND b.ifscCode = :ifscCode")
    Optional<BeneficiaryDetails> findDuplicateBeneficiary(
            @Param("customerId") Integer customerId,
            @Param("accountNumber") String accountNumber,
            @Param("ifscCode") String ifscCode
    );

    /**
     * Count active beneficiaries for a customer
     */
    @Query("SELECT COUNT(b) FROM BeneficiaryDetails b WHERE b.customerId = :customerId " +
            "AND b.beneficiaryStatus = 'ACTIVE'")
    long countActiveBeneficiaries(@Param("customerId") Integer customerId);

    /**
     * Find all pending activation beneficiaries for a customer
     */
    @Query("SELECT b FROM BeneficiaryDetails b WHERE b.customerId = :customerId " +
            "AND b.beneficiaryStatus = 'PENDING'")
    List<BeneficiaryDetails> findPendingActivationBeneficiaries(@Param("customerId") Integer customerId);

    /**
     * Find beneficiary by account number
     */
    Optional<BeneficiaryDetails> findByBeneficiaryAccountNumber(String beneficiaryAccountNumber);
}
