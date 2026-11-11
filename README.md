# Bank Transfer System

A web-based banking application developed using **Spring Boot, Thymeleaf, and MySQL**. The system allows users to manage customer accounts and perform banking transactions.

## Features

- View customer accounts and account balances.
- Add new customers.
- Transfer money between accounts.
- Apply a **1% bank transfer fee**, credited to the Bank Fees account (`999999`).
- Deposit and withdraw money.
- View transaction history.
- View large transactions.
- Handle failed transactions using exception handling.
- Ensure transaction consistency using Spring `@Transactional` and rollback.
- Record failed transactions using `REQUIRES_NEW`.

## Technologies Used

- Java
- Spring Boot
- Spring Data JPA
- Thymeleaf
- MySQL
- Maven
- HTML and CSS

## Database Configuration

Create a MySQL database named:

```sql
CREATE DATABASE bank_transfer_db;
```

Configure your database username and password in `application.properties`.

## Running the Application

1. Clone the repository.
2. Open the project in IntelliJ IDEA or VS Code.
3. Configure the MySQL database connection.
4. Run the Spring Boot application.
5. Open your browser and visit:

```text
http://localhost:8080
```

## Default Accounts

| Account Number | Customer Name | Initial Balance |
|---|---|---:|
| 100001 | Kamal Perera | LKR 50,000 |
| 100002 | Nimali Silva | LKR 25,000 |
| 100003 | Saman Kumara | LKR 10,000 |
| 999999 | Bank Fees | LKR 0 |

## Transaction Fee

A 1% fee is deducted from the sender in addition to the transfer amount and credited to the Bank Fees account.

All three balance updates are handled within a single transaction to maintain data consistency.

---

**Course:** COSC 32133 / BECS 32263 – Full-Stack Software Development
