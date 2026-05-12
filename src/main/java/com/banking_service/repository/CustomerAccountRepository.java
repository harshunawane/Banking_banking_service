package com.banking_service.repository;

import com.banking_service.entity.CustomerAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CustomerAccountRepository extends JpaRepository<CustomerAccount, Integer> {

    Optional<CustomerAccount> findByAccountNumber(String accountNumber);

    Optional<CustomerAccount> findByCustomerId(Integer customerId);
}

