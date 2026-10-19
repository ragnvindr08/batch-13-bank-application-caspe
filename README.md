
# Ito yung guide
# Banking Application (Spring Boot + Spring Data JPA + XAMPP MySQL)

Console banking app with 7 features: Create Account, Balance Inquiry, List Accounts,
Deposit, Withdraw, Transfer, Transaction History.

## 1. Database (XAMPP)
1. Open the XAMPP Control Panel and **Start** MySQL (and Apache for phpMyAdmin).
2. Open http://localhost/phpmyadmin > **Import** > choose `sql/schema.sql` > **Import**.
3. Check that `banking_db` has the tables `accounts` and `transactions`.

Connection settings are in `src/main/resources/application.properties`
(user `root`, empty password, port 3306). Change the password there if you set one.

## 2. Run in VS Code
1. Install a JDK 21 and the **Extension Pack for Java** (it includes Spring Boot support via Maven).
2. **File > Open Folder** and choose this `banking-app` folder.
3. Wait for the Maven import to finish (it downloads Spring Boot and the MySQL driver).
4. Open `src/main/java/com/bank/banking_app/BankingAppApplication.java` and click **Run**
   above `main`. Type your answers to the menu in the **Terminal** tab.

Or from a terminal inside this folder:

    Windows:     mvnw.cmd spring-boot:run
    Mac/Linux:   ./mvnw spring-boot:run

## 3. How the code is organised
    src/main/java/com/bank/banking_app/
    ├── BankingAppApplication.java      starts Spring Boot
    ├── console/ConsoleMenu.java        the menu (CommandLineRunner)
    ├── service/BankService.java        business rules, validation, @Transactional
    ├── repository/                     AccountRepository, TransactionRepository (Spring Data JPA)
    ├── model/                          Account, Transaction (@Entity)
    └── exception/                      InsufficientFundsException

- `@Transactional` on `BankService` replaces the manual commit/rollback: if any step of a
  transfer fails, nothing is saved.
- All queries use parameters (Spring Data / JPQL `:name`), so SQL injection is not possible.

## Troubleshooting
- "Communications link failure": MySQL is not started in XAMPP.
- "Unknown database 'banking_db'": the schema was not imported yet.
- Peso sign shows as `Γé▒`: in Command Prompt run `chcp 65001` first
  (PowerShell: `[Console]::OutputEncoding = [System.Text.Encoding]::UTF8`).
- The default Spring Boot test was removed because it would start the menu and wait for keyboard input.
