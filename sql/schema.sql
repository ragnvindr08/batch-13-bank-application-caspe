-- Banking Application database script (MySQL)
CREATE DATABASE IF NOT EXISTS banking_db;
USE banking_db;

CREATE TABLE IF NOT EXISTS accounts (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    account_number VARCHAR(20)   NOT NULL UNIQUE,
    account_name   VARCHAR(100)  NOT NULL,
    balance        DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    created_at     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS transactions (
    id                    INT AUTO_INCREMENT PRIMARY KEY,
    transaction_reference VARCHAR(20)   NOT NULL UNIQUE,
    account_number        VARCHAR(20)   NOT NULL,
    transaction_type      VARCHAR(10)   NOT NULL,  -- DEPOSIT, WITHDRAW, TRANSFER
    amount                DECIMAL(15,2) NOT NULL,
    balance               DECIMAL(15,2) NOT NULL,  -- balance after the transaction
    reference_account     VARCHAR(20),             -- other account in a transfer
    created_at            TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_number) REFERENCES accounts(account_number)
);
