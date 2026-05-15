package com.banking_service.repository;


import com.banking_service.entity.TransactionDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionDetails, Integer> {

    /**
     * Find transactions by customer ID and date range for daily limit validation
     */
    @Query("SELECT t FROM TransactionDetails t WHERE t.customerId = :customerId AND DATE(t.createdTime) = DATE(:date)")
    List<TransactionDetails> findTransactionsByCustomerAndDate(@Param("customerId") Long customerId, @Param("date") LocalDateTime date);

    /**
     * Calculate total transaction amount for customer on specific date
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM TransactionDetails t WHERE t.customerId = :customerId AND DATE(t.createdTime) = DATE(:date) AND t.transactionStatus = 'SUCCESS'")
    BigDecimal getTotalTransactionAmountByCustomerAndDate(@Param("customerId") Long customerId, @Param("date") LocalDateTime date);

    /**
     * Find recent transactions for rate limiting (last 1 minute) - per beneficiary
     */
    @Query("SELECT COUNT(t) FROM TransactionDetails t WHERE t.customerId = :customerId AND t.beneficiaryId = :beneficiaryId AND t.createdTime >= :since")
    long countRecentTransactionsByBeneficiary(@Param("customerId") Long customerId, @Param("beneficiaryId") Long beneficiaryId, @Param("since") LocalDateTime since);

    /**
     * Check for duplicate request based on reference number
     */
    Optional<TransactionDetails> findByReferenceNumber(String referenceNumber);

    /**
     * Find transaction history for customer
     */
    List<TransactionDetails> findByCustomerIdOrderByCreatedTimeDesc(Long customerId);

    /**
     * Find transaction by reference number
     */
  //  Optional<TransactionDetails> findByReferenceNumber(String referenceNumber);
}
