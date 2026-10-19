package com.bank.banking_app.repository;

import com.bank.banking_app.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

    // Spring builds: ... WHERE account_number = ? ORDER BY id ASC
    List<Transaction> findByAccountNumberOrderByIdAsc(String accountNumber);
}
