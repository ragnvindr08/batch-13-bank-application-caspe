package com.bank.banking_app.repository;

import com.bank.banking_app.model.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
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

    // PESSIMISTIC_WRITE = SELECT ... FOR UPDATE.
    // The row is locked until the transaction ends, so two users cannot change the same balance at once.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.accountNumber = :accountNumber")
    Optional<Account> findForUpdate(@Param("accountNumber") String accountNumber);
}
