### Testing Guide - Multi-Chain Wallet
This guide provides comprehensive instructions for running the test suite for the Multi-Chain Wallet application. Our tests are designed to ensure code quality, correctness, and reliability across all layers of the application.

## Table of Contents
1. Prerequisites
2. Quick Start: Running All Tests
3. Running Specific Tests
4. Test Coverage
5. Test Categories
6. Test Configuration
7. Continuous Integration (CI)
8. Troubleshooting Tests

## Prerequisites
Before running tests, ensure you have the following installed:

- Java 17 (Download)
- Maven 3.8+ (Download) 

You do not need Docker or a running PostgreSQL instance to run the tests, as they use an in-memory H2 database.

### Verify Installation
``` Bash

# Check Java version
java -version
# Expected: openjdk version "17.x.x"

# Check Maven version
mvn -version
# Expected: Apache Maven 3.8.x or higher
``` 

## Quick Start: Running All Tests
This is the simplest way to validate the entire application.

```Bash

# This command will clean the project, download dependencies,
# compile the code, and run all unit and integration tests.
mvn clean test
```
A successful run will end with:

```text

[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

### Running Specific Tests
For faster feedback during development, you can run a subset of tests.

Run a Single Test Class
Use the -Dtest flag with the class name.

```Bash

# Run all tests in UserServiceTest.java
mvn test -Dtest=UserServiceTest

# Run all tests in the controller package
mvn test -Dtest=com.wallet.controller.*
```
### Run a Single Test Method
Specify the class and method name.

```Bash

# Run the testRegisterUser_Success method in UserServiceTest
mvn test -Dtest=UserServiceTest#testRegisterUser_Success
```
### Run Tests Based on a Pattern
Run all test classes that match a specific pattern.

```Bash

# Run all service tests
mvn test -Dtest=*ServiceTest

# Run all integration tests
mvn test -Dtest=*IntegrationTest
```
### Exclude Tests
Use an exclamation mark ! to exclude tests.

```Bash

# Run all tests except integration tests
mvn test -Dtest="!*IntegrationTest"
```
### Skip Tests
Sometimes you need to build the application without running tests.

```Bash

# Skip tests entirely (recommended for quick builds)
mvn clean package -DskipTests

# Alternative: Skip test compilation
mvn clean package -Dmaven.test.skip=true
```
### Test Coverage
We use JaCoCo to measure test coverage.

#### Generate a Coverage Report
Run the tests with the JaCoCo plugin:

```Bash

mvn clean test jacoco:report
```
Open the HTML report in your browser:

```Bash

# On macOS
open target/site/jacoco/index.html

# On Linux
xdg-open target/site/jacoco/index.html

# On Windows
start target/site/jacoco/index.html
```
This report provides a detailed breakdown of line, branch, and method coverage for each class.

### Enforce Coverage Thresholds
The build will fail if coverage drops below the configured threshold (e.g., 80%).

```Bash

# This command runs tests and then checks coverage rules
mvn clean verify
```
## Test Categories
Our test suite is divided into several categories to ensure thorough testing at different levels.

### 1. Unit Tests
   - Location: src/test/java/com/wallet/service, src/test/java/com/wallet/model, etc.
   - Purpose: Test individual classes (services, models) in isolation.
   - Technology: JUnit 5, Mockito.
   - How to Run: mvn test -Dtest=*ServiceTest
   These tests are fast and do not require the Spring context to load. External dependencies are mocked.

### 2. Repository Tests
   - Location: src/test/java/com/wallet/repository
   - Purpose: Verify the data access layer (JPA Repositories).
   - Technology: @DataJpaTest, H2 in-memory database.
   - How to Run: mvn test -Dtest=*RepositoryTest
   These tests use a real (but in-memory) database to ensure queries and entity mappings are correct.

### 3. Integration Tests
   - Location: src/test/java/com/wallet/controller
   - Purpose: Test the full application stack, from the API endpoint to the database.
   - Technology: @SpringBootTest, MockMvc.
   - How to Run: mvn test -Dtest=*IntegrationTest
   These tests simulate HTTP requests and verify the entire flow of an API call. They are slower but provide the highest confidence.

### 4. Security Tests
   - Location: src/test/java/com/wallet/security
   - Purpose: Test security components like JWT generation and validation.
   - Technology: JUnit 5.
   - How to Run: mvn test -Dtest=*SecurityTest

## Test Configuration
   - Test Profile: All tests run using the test Spring profile.
   - Configuration File: src/test/resources/application-test.yml.
   - Database: An H2 in-memory database is used for all tests. This ensures:
   - Speed: No slow disk I/O.
   - Isolation: Tests do not interfere with your local or production database.
   - Consistency: The database is created fresh for each test run, providing a clean slate.
   - Random Ports: The web server starts on a random available port to prevent conflicts during parallel test execution (server.port: 0).
   
## Continuous Integration (CI)
   Our CI pipeline (e.g., GitHub Actions) automatically runs all tests on every push and pull request to the main and develop branches.

#### Example GitHub Actions Workflow (.github/workflows/tests.yml)
```YAML
name: Run Tests and Coverage

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: maven
      - name: Run tests with Maven
        run: mvn -B test
      - name: Generate coverage report
        run: mvn jacoco:report
      - name: Upload coverage to Codecov
        uses: codecov/codecov-action@v4
        with:
          token: ${{ secrets.CODECOV_TOKEN }}
          files: ./target/site/jacoco/jacoco.xml
```
This ensures that no code is merged without passing all tests and meeting coverage standards.

## Troubleshooting Tests
### Issue: Lombok errors (cannot find symbol for get..., set...) \
- Solution: Ensure you have the Lombok plugin installed and annotation processing enabled in your IDE (IntelliJ, Eclipse, etc.). Then, run mvn clean install. 

### Issue: Tests are slow
- Solution: Run specific tests using the -Dtest flag. Ensure your unit tests are not loading the full Spring context unless necessary.

### Issue: H2 database connection errors
- Solution: Check application-test.yml for correct H2 configuration. Ensure no other process is conflicting with H2's default settings. Run mvn clean to ensure no old configurations are cached.

### Issue: Flaky tests (sometimes pass, sometimes fail)
- Solution: This often indicates a race condition or a dependency on test execution order. \
Ensure each test is fully independent. Use @BeforeEach to set up a clean state. \
For async tests, use libraries like Awaitility to wait for conditions instead of using Thread.sleep().