package com.banking_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "customer_account")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer customerId;

    @Column(unique = true, nullable = false)
    private String accountNumber;

    @Column(nullable = false)
    private String accountHolderName;

    @Column(nullable = false)
    private String accountStatus;

    @Column(nullable = false)
    private String bankAccountName;

    @Column(name = "available_balance", precision = 15, scale = 2)  // Created because of US BNK-1082 Need customer balance
    private BigDecimal availableBalance;
}

