# hybrid-threat-simple-api-tests

A small, self-contained demonstration of Java REST API testing using **REST Assured** and **direct JDBC assertions** against **PostgreSQL**. 

The repository includes a lightweight **Spring Boot** mock "hybrid threat" API as the system under test (SUT). It was built as a portfolio project demonstrating modern Java testing practices, including REST API verification, direct SQL assertions, and CI pipeline integration.

## Stack & Versions

This project uses the latest stable GA releases to ensure compatibility with modern Java environments.

| Component | Version | Notes |
|---|---|---|
| **Java** | 25 (LTS) | Latest long-term support release. |
| **Maven** | 3.9+ | Built and tested with 3.10.0. |
| **Spring Boot** | 4.0.7 | Uses JUnit 6 by default and Jackson 3 (`tools.jackson.databind`). |
| **REST Assured** | 6.2.0 | Latest release, fully Java-based (no Groovy dependency). |
| **JUnit** | 6.x | Managed automatically by the Spring Boot 4 parent POM. |
| **PostgreSQL JDBC** | 42.7.13 | Latest PostgreSQL driver. |
| **PostgreSQL** | 17 | Tested locally and in CI. |

## What's in the Box?

*   **Mock API** (`src/main/java`) — Spring Boot + `JdbcClient` CRUD operations over `/api/threats`.
*   **API Tests** (`ThreatCrudTest`, `ThreatValidationTest`) — REST Assured for HTTP-level testing.
*   **Database Tests** (`ThreatDbTest`) — Plain `DriverManager` JDBC assertions to verify the API actually persists data.
*   **CI Pipeline** — GitHub Actions workflow that spins up a Postgres service container and runs `mvn test`.

## Directory Layout

```text
.
├── pom.xml
├── README.md
├── .github/workflows/ci.yml
├── src/main/resources/
│   ├── application.properties
│   ├── schema.sql
│   └── data.sql
├── src/main/java/com/udaiyan/hybridthreat/
│   ├── HybridThreatApplication.java
│   ├── model/{Threat,ThreatType}.java
│   ├── repository/ThreatRepository.java
│   └── controller/ThreatController.java
└── src/test/
    ├── java/com/udaiyan/hybridthreat/
    │   ├── BaseApiTest.java
    │   ├── ThreatCrudTest.java
    │   ├── ThreatValidationTest.java
    │   └── ThreatDbTest.java
    └── resources/application-test.properties
```

## Local Setup

### 1. Prerequisites
*   **Java 25** installed (e.g., Microsoft OpenJDK).
*   **PostgreSQL 17** running locally on port `5432`.
*   **Maven** installed.

### 2. Create the Databases
The project uses **two separate databases** to ensure that running destructive tests does not wipe out manual application data.
Open your terminal or use pgAdmin/IntelliJ to create them:
```bash
createdb -U postgres hybridthreat
createdb -U postgres hybridthreat_test
```
*Note: The default credentials expected are `postgres` / `postgres`. If your local setup uses a different password, update both `application.properties` and `application-test.properties`.*

### 3. Run the Application
To start the mock API:
```bash
mvn spring-boot:run
```
You can now browse the API in your browser:
*   `http://localhost:8080/api/threats`
*   `http://localhost:8080/api/threats?type=HYBRID`

### 4. Run the Tests
To execute the full test suite:
```bash
mvn test
```
Surefire reports are written to `target/surefire-reports/`.

---

## Testing Strategy

### REST Assured vs. Plain JDBC
This repo demonstrates two layers of verification:
1.  **API-Level Tests**: REST Assured 6.2.0 handles the HTTP requests. It uses the `given() / when() / then()` DSL to assert status codes, headers, and JSON response bodies. We use Jackson 3 (`tools.jackson.databind.ObjectMapper`) to serialize our Java POJOs into JSON payloads.
2.  **Database-Level Tests**: `ThreatDbTest` bypasses the API and connects directly to Postgres using standard `java.sql.DriverManager` (no HikariCP, no ORM). After a POST, it queries the database directly to prove the row exists.

### Seed Data
`src/main/resources/data.sql` seeds exactly six known threats on every startup (`spring.sql.init.mode=always`) and before every single test method (via `@Sql` on `BaseApiTest`). 

The script begins with `TRUNCATE TABLE threats RESTART IDENTITY CASCADE`, guaranteeing a clean slate for every test run.

| ID | Name | Type | Severity | Status |
|----|----------------------------------------|----------|----------|----------|
| 1  | Operation Cobalt Drift                 | CYBER    | CRITICAL | ACTIVE |
| 2  | Port facility reconnaissance           | PHYSICAL | HIGH     | OPEN |
| 3  | Combined cyber-kinetic exercise        | HYBRID   | CRITICAL | MITIGATED|
| 4  | Phishing campaign against MOD staff    | CYBER    | MEDIUM   | OPEN |
| 5  | Suspicious drone activity near site    | PHYSICAL | LOW      | CLOSED |
| 6  | Coordinated disinformation + DDoS      | HYBRID   | HIGH     | ACTIVE |

**Why seed data?**
Real-world test suites rarely run against an empty database. Seeding allows tests to assert against known baselines (`size() == SEED_COUNT + N`), read fixed records (`GET /api/threats/1`), and properly test filtering logic (`?type=HYBRID` returns exactly 2 rows).

### REST Assured State Safety
When testing multiple endpoints sequentially (e.g., a DELETE followed by a GET), REST Assured's `RequestSpecification` can mutate if reused directly. All tests in this repo use `given().spec(requestSpec).pathParam(...)` to ensure a fresh, isolated request context for every single HTTP call.

---

## CI/CD Pipeline

The GitHub Actions workflow (`.github/workflows/ci.yml`) runs on every push to `main` and on PRs. 
It automatically:
1. Spins up a Postgres 17 service container.
2. Sets up Temurin JDK 25.
3. Runs `mvn -B test`.

Because the test properties match the CI Postgres service container credentials, no secrets or environment variables are required in GitHub.

## Non-Goals (Deliberate Exclusions)
*   **No BDD/Cucumber**: This repo focuses on clean JUnit 6 tests to keep the boilerplate minimal, avoiding the overhead of feature files and step definitions.
*   **No HikariCP in tests**: Plain `DriverManager` is used for JDBC assertions to demonstrate raw SQL/Java connectivity, as connection pooling provides no benefit for a single-threaded test suite.
*   **No Testcontainers**: We connect to a local Postgres instance via standard JDBC URLs to reflect a traditional enterprise testing setup.
