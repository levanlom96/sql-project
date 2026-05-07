# SQL Transactions – Product / Depot / Stock

A Java + PostgreSQL demonstration of six ACID-compliant database transactions using JDBC.  
Reactive constraints (`ON DELETE CASCADE` / `ON UPDATE CASCADE`) keep the `Stock` table consistent automatically.

---

## Prerequisites

| Tool | Version |
|------|---------|
| Java | 11 or later |
| Maven | 3.6 or later |
| Docker | any recent version |

---

## Quick Start

### 1. Start a PostgreSQL container

```bash
docker run --name pgdemo \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -d postgres:16
```

### 2. Create the database

```bash
docker exec -it pgdemo psql -U postgres -c "CREATE DATABASE projectdb;"
```

### 3. Clone this repository

```bash
git clone https://github.com/<your-username>/<your-repo>.git
cd <your-repo>
```

### 4. Load the schema and seed data

```bash
docker exec -i pgdemo psql -U postgres -d projectdb < sql/schema.sql
docker exec -i pgdemo psql -U postgres -d projectdb < sql/data.sql
```

### 5. Build the project

```bash
mvn clean package -q
```

### 6. Run the transactions

```bash
java -jar target/sql-transactions.jar
```

You will see the state of all three tables printed before and after each transaction.

---

## Configuration

If your PostgreSQL instance uses different credentials or a different port, edit the constants at the top of `src/main/java/transactions/DBConnection.java`:

```java
private static final String HOST     = "localhost";
private static final String PORT     = "5432";
private static final String DATABASE = "projectdb";
private static final String USER     = "postgres";
private static final String PASSWORD = "postgres";
```

---

## Transactions

| # | Description |
|---|-------------|
| 1 | Delete product `p1` from `Product` (cascades to `Stock`) |
| 2 | Delete depot `d1` from `Depot` (cascades to `Stock`) |
| 3 | Rename product `p1` → `pp1` in `Product` (cascades to `Stock`) |
| 4 | Rename depot `d1` → `dd1` in `Depot` (cascades to `Stock`) |
| 5 | Add product `(p100, cd, 5)` to `Product` and `(p100, d2, 50)` to `Stock` |
| 6 | Add depot `(d100, Chicago, 100)` to `Depot` and `(p1, d100, 100)` to `Stock` |

---

## Project Structure

```
.
├── pom.xml                              Maven build file
├── README.md                            This file
├── run.sh                               Build & run the project entirely inside Docker
├── sql/
│   ├── schema.sql                       CREATE TABLE statements with CASCADE constraints
│   └── data.sql                         Initial seed data
└── src/main/java/transactions/
    ├── DBConnection.java                JDBC connection factory
    └── Main.java                        All six transactions
```

---

## How ACID is Implemented

- **Atomicity** – `conn.setAutoCommit(false)` + `conn.rollback()` on any error ensures all-or-nothing execution.  
- **Consistency** – `NOT NULL` and foreign-key constraints enforced by PostgreSQL; CASCADE rules keep `Stock` in sync.  
- **Isolation** – Each transaction uses its own `Connection` under PostgreSQL's default *Read Committed* isolation level.  
- **Durability** – `conn.commit()` flushes changes to the PostgreSQL Write-Ahead Log (WAL).
