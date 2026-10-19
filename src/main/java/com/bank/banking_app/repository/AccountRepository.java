package com.bank.banking_app.repository;

import com.bank.banking_app.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA writes the SQL for us. We only declare the methods.
 * All values are passed as parameters, so SQL injection is not possible.
 */
public interface AccountRepository extends JpaRepository<Account, Integer> {

    // Spring builds the query from the method name: ... WHERE account_number = ?
    boolean existsByAccountNumber(String accountNumber);

    Optional<Account> findByAccountNumber(String accountNumber);

    // Spring builds: ... ORDER BY account_number ASC
    List<Account> findAllByOrderByAccountNumberAsc();

    // PESSIMISTIC_WRITE gagamit ng native query para gumana sa MariaDB / MySQL
    @Query(value = "SELECT * FROM accounts WHERE account_number = :accountNumber FOR UPDATE", nativeQuery = true)
    Optional<Account> findForUpdate(@Param("accountNumber") String accountNumber);
}