package com.bank.banking_app.service;

import com.bank.banking_app.exception.InsufficientFundsException;
import com.bank.banking_app.model.Account;
import com.bank.banking_app.model.Transaction;
import com.bank.banking_app.repository.AccountRepository;
import com.bank.banking_app.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Business rules and validation of the Banking Application.
 *
 * @Transactional makes every method ONE database transaction:
 *   - if the method finishes normally, Spring commits all changes
 *   - if the method throws an exception, Spring rolls back everything
 * (rollbackFor = Exception.class is needed because InsufficientFundsException is a checked
 *  exception, and Spring only rolls back on unchecked exceptions by default.)
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class BankService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    // Constructor: Spring gives us the two repositories automatically (constructor injection).
    public BankService(AccountRepository accountRepository,
                       TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    // =====================================================================
    // 1. CREATE ACCOUNT
    // =====================================================================

    // Overloading: same method name as below, but with only two parameters.
    // A new account without an initial deposit starts with a balance of zero.
    public Account createAccount(String accountNumber, String accountName) {
        return createAccount(accountNumber, accountName, BigDecimal.ZERO);
    }

    public Account createAccount(String accountNumber, String accountName, BigDecimal initialDeposit) {

        // Validation
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Account number must not be empty.");
        }
        if (accountName == null || accountName.trim().isEmpty()) {
            throw new IllegalArgumentException("Account name must not be empty.");
        }
        if (initialDeposit.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Initial deposit must be zero or greater.");
        }

        accountNumber = accountNumber.trim();
        accountName = accountName.trim();

        if (accountRepository.existsByAccountNumber(accountNumber)) {
            throw new IllegalArgumentException("Account number already exists.");
        }

        Account account = accountRepository.save(new Account(accountNumber, accountName, initialDeposit));

        // The initial deposit is recorded as the first transaction of the account.
        if (initialDeposit.compareTo(BigDecimal.ZERO) > 0) {
            saveTransaction(accountNumber, "DEPOSIT", initialDeposit, initialDeposit, null);
        }
        return account;
    }

    // =====================================================================
    // 2. BALANCE INQUIRY
    // =====================================================================
    @Transactional(readOnly = true)
    public Account getAccount(String accountNumber) {
        return findExistingAccount(accountNumber, "Account");
    }

    // =====================================================================
    // 3. LIST ACCOUNTS
    // =====================================================================
    @Transactional(readOnly = true)
    public List<Account> listAccounts() {
        return accountRepository.findAllByOrderByAccountNumberAsc();
    }

    // =====================================================================
    // 4. DEPOSIT
    // =====================================================================

    // Overloading: same method name, but the amount is a double instead of a BigDecimal.
    public BigDecimal deposit(String accountNumber, double amount) {
        return deposit(accountNumber, BigDecimal.valueOf(amount));
    }

    // Returns the PREVIOUS balance. The new balance is previous balance + amount.
    public BigDecimal deposit(String accountNumber, BigDecimal amount) {
        checkAmountIsPositive(amount);

        Account account = findExistingAccountForUpdate(accountNumber, "Account");

        BigDecimal previousBalance = account.getBalance();
        BigDecimal newBalance = previousBalance.add(amount);

        account.setBalance(newBalance);   // saved automatically when the transaction commits
        saveTransaction(account.getAccountNumber(), "DEPOSIT", amount, newBalance, null);

        return previousBalance;
    }

    // =====================================================================
    // 5. WITHDRAW
    // =====================================================================

    // Overloading: same method name, but the amount is a double.
    public BigDecimal withdraw(String accountNumber, double amount) throws InsufficientFundsException {
        return withdraw(accountNumber, BigDecimal.valueOf(amount));
    }

    // Returns the PREVIOUS balance. The new balance is previous balance - amount.
    public BigDecimal withdraw(String accountNumber, BigDecimal amount) throws InsufficientFundsException {
        checkAmountIsPositive(amount);

        Account account = findExistingAccountForUpdate(accountNumber, "Account");

        BigDecimal previousBalance = account.getBalance();
        checkEnoughBalance(previousBalance, amount);

        BigDecimal newBalance = previousBalance.subtract(amount);

        account.setBalance(newBalance);
        saveTransaction(account.getAccountNumber(), "WITHDRAW", amount, newBalance, null);

        return previousBalance;
    }

    // =====================================================================
    // 6. TRANSFER
    // =====================================================================
    // Both balance updates and both transaction records succeed together, or none of them do.
    public void transfer(String fromNumber, String toNumber, BigDecimal amount)
            throws InsufficientFundsException {

        fromNumber = fromNumber.trim();
        toNumber = toNumber.trim();

        if (fromNumber.equals(toNumber)) {
            throw new IllegalArgumentException("Source and destination accounts must be different.");
        }
        checkAmountIsPositive(amount);

        // Always lock the accounts in the same order (smaller number first)
        // so two transfers at the same time cannot block each other forever.
        Account from;
        Account to;
        if (fromNumber.compareTo(toNumber) < 0) {
            from = findExistingAccountForUpdate(fromNumber, "Source account");
            to = findExistingAccountForUpdate(toNumber, "Destination account");
        } else {
            to = findExistingAccountForUpdate(toNumber, "Destination account");
            from = findExistingAccountForUpdate(fromNumber, "Source account");
        }

        checkEnoughBalance(from.getBalance(), amount);

        BigDecimal fromNewBalance = from.getBalance().subtract(amount);
        BigDecimal toNewBalance = to.getBalance().add(amount);

        // Update both balances
        from.setBalance(fromNewBalance);
        to.setBalance(toNewBalance);

        // Save one transaction record for each account
        saveTransaction(fromNumber, "TRANSFER", amount, fromNewBalance, toNumber);
        saveTransaction(toNumber, "TRANSFER", amount, toNewBalance, fromNumber);
    }

    // =====================================================================
    // 7. TRANSACTION HISTORY
    // =====================================================================
    @Transactional(readOnly = true)
    public List<Transaction> getHistory(String accountNumber) {
        Account account = findExistingAccount(accountNumber, "Account");
        return transactionRepository.findByAccountNumberOrderByIdAsc(account.getAccountNumber());
    }

    // =====================================================================
    // HELPER METHODS (small methods that remove repeated code)
    // =====================================================================

    // Saves a transaction record with the next reference: TXN-001, TXN-002, ...
    private void saveTransaction(String accountNumber, String type, BigDecimal amount,
                                 BigDecimal balanceAfter, String referenceAccount) {
        String reference = String.format("TXN-%03d", transactionRepository.count() + 1);
        transactionRepository.save(
                new Transaction(reference, accountNumber, type, amount, balanceAfter, referenceAccount));
    }

    // Finds an account. If it does not exist, stops with a clear error message.
    private Account findExistingAccount(String accountNumber, String label) {
        return accountRepository.findByAccountNumber(accountNumber.trim())
                .orElseThrow(() -> new IllegalArgumentException(
                        label + " " + accountNumber.trim() + " does not exist."));
    }

    // Same as above, but also locks the row so nobody else can change it until the transaction ends.
    private Account findExistingAccountForUpdate(String accountNumber, String label) {
        return accountRepository.findForUpdate(accountNumber.trim())
                .orElseThrow(() -> new IllegalArgumentException(
                        label + " " + accountNumber.trim() + " does not exist."));
    }

    // Rule: the amount of a deposit, withdrawal or transfer must be greater than zero.
    private void checkAmountIsPositive(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }
    }

    // Rule: a withdrawal or transfer must not be more than the available balance.
    private void checkEnoughBalance(BigDecimal balance, BigDecimal amount)
            throws InsufficientFundsException {
        if (amount.compareTo(balance) > 0) {
            throw new InsufficientFundsException("Insufficient balance. Available: " + balance);
        }
    }
}
