DEMOBLAZE SELENIUM JAVA AUTOMATION PROJECT


PROJECT OVERVIEW

This project contains an automated Selenium + Java + TestNG test suite for the
public demo shopping site https://demoblaze.com.
It covers the five required test scenarios (TC01-TC05): home page smoke test,
product selection, add to cart, cart management (add/remove/verify), and
checkout validation (invalid submission followed by a valid submission).


PREREQUISITES

1. Java JDK 11 or higher installed and on PATH   (check: java -version)
2. Apache Maven 3.6+ installed and on PATH        (check: mvn -version)
3. Google Chrome browser installed (any recent version)
4. An active internet connection
   - required to reach https://demoblaze.com during test execution
   - required the first time Maven downloads dependencies from Maven Central
   - WebDriverManager (io.github.bonigarcia) automatically downloads the
     matching ChromeDriver binary for the installed Chrome version, so a
     separate manual chromedriver download/setup is NOT required.


PROJECT STRUCTURE

pom.xml                                  - Maven dependencies (Selenium, TestNG, WebDriverManager)
testng.xml                               - TestNG suite file (referenced by surefire)
src/test/java/com/sandun/demoblaze/
    BaseTest.java                        - WebDriver setup/teardown, base URL, reusable constants,
                                            screenshot capture, and failure page-source dump
    DemoBlazeTests.java                  - The 5 required @Test methods (TC01-TC05) plus a
                                            clickWithRetry() helper for resilient element clicks
screenshots/                             - Screenshot captured after every test run (pass or fail)
target/failure-pagesource/               - HTML snapshot saved only when a test fails
target/surefire-reports/                 - TestNG HTML/XML execution reports from the last run


HOW TO RUN THE TESTS

1. Extract the project and open a terminal in the project's root folder
   (the folder containing pom.xml).
2. Run:
       mvn clean test
3. Maven will:
   - download all dependencies (first run only)
   - compile the test sources
   - launch Chrome (WebDriverManager sets up the matching ChromeDriver)
   - execute all 5 tests defined in testng.xml
   - print console output for each test (alert text, prices, cart
     contents, retry/fallback messages, etc.)

4. After the run finishes:
   - Screenshot evidence (every test, pass or fail): screenshots/
   - TestNG HTML report:               target/surefire-reports/index.html
   - TestNG XML result summary:        target/surefire-reports/testng-results.xml
   - Failure-only page source dumps:   target/failure-pagesource/ (created only if a test fails)

To run a single test class only:
       mvn -Dtest=DemoBlazeTests test


IMPLEMENTATION NOTES SPECIFIC TO THIS PROJECT

- WebDriverWait is set to 20 seconds (BaseTest.wait) to absorb slow responses
  from the shared public demo backend, instead of using Thread.sleep().
- clickWithRetry(locator, attempts) in DemoBlazeTests wraps repeated element
  interactions (Phones link, product links, Add to cart, cart icon, Place
  Order, Purchase). It waits for presence, scrolls the element into view via
  JavascriptExecutor, then tries elementToBeClickable(); if that times out it
  falls back to a JavaScript click instead of failing immediately. This makes
  the suite more resilient to demoblaze.com's occasional slow rendering.
- @AfterMethod(alwaysRun = true) in BaseTest takes a screenshot after every
  test (named "<testName>-PASS-<timestamp>.png" or "...-FAIL-...") and, only
  on failure, additionally saves the full page source under
  target/failure-pagesource/ together with the current URL, before quitting
  the driver.
- The checkout form field ids used are: name, country, city, card, month,
  year (the credit-card field's id on the live site is "card").
- The "PRODUCT STORE" check in TC01 targets the navbar brand link
  (//a[contains(text(),'PRODUCT STORE')]), not a page heading.
- The add-to-cart confirmation alert text is asserted as exactly
  "Product added" (no trailing period, as returned by the live site).


ASSUMPTIONS

- demoblaze.com is a shared public demo backend and can occasionally be slow
  or briefly unresponsive; the 20-second explicit wait plus clickWithRetry()
  are used to absorb this instead of fixed delays.
- Checkout uses only fictitious test data (name, address, and a well-known
  dummy test card number: 4111111111111111). No real personal or payment
  information is used anywhere in this project.
- Locators (including the "card" field id and the PRODUCT STORE anchor) were
  verified against the live site at the time of writing; if demoblaze.com
  changes its markup in future, locators in DemoBlazeTests.java may need
  updating.
- Tests were last executed successfully with all 5/5 test methods passing
  (see target/surefire-reports/testng-results.xml and the screenshots/
  folder for the recorded run evidence).
