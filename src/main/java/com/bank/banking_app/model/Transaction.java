package com.bank.banking_app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents one row of the "transactions" table.
 */
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "transaction_reference", nullable = false, unique = true, length = 20)
    private String reference;                // example: TXN-001

    @Column(name = "account_number", nullable = false, length = 20)
    private String accountNumber;

    @Column(name = "transaction_type", nullable = false, length = 10)
    private String type;                     // DEPOSIT, WITHDRAW or TRANSFER

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal balanceAfter;         // balance of the account after this transaction

    @Column(name = "reference_account", length = 20)
    private String referenceAccount;         // the other account in a transfer (null if none)

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // JPA needs an empty constructor. Our own code does not use it.
    protected Transaction() {
    }

    public Transaction(String reference, String accountNumber, String type,
                       BigDecimal amount, BigDecimal balanceAfter, String referenceAccount) {
        this.reference = reference;
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.referenceAccount = referenceAccount;
    }

    @PrePersist
    private void setCreatedAt() {
        this.createdAt = LocalDateTime.now();
    }

    public String getReference() {
        return reference;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public String getReferenceAccount() {
        return referenceAccount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Overriding: replaces the toString() method inherited from Object.
    @Override
    public String toString() {
        return reference + " " + type + " " + amount + " (balance: " + balanceAfter + ")";
    }
}
