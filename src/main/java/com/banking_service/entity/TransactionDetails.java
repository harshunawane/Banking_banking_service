package com.banking_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fund_transfer_transaction")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer transactionId;

    @Column(unique = true, nullable = false, length = 100)
    private String referenceNumber;

    @Column(nullable = false)
    private Integer customerId;

    @Column(nullable = false)
    private Integer beneficiaryId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal charges;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal gst;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalDebitAmount;

    @Column(nullable = false, length = 20)
    private String transactionStatus;

    @Column(length = 255)
    private String remarks;

    @Column(nullable = false, length = 20)
    private String transferType;

    @Column(length = 255)
    private String failureReason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdTime;

    @PrePersist
    protected void onCreate() {
        if (createdTime == null) {
            createdTime = LocalDateTime.now();
        }
    }
}
