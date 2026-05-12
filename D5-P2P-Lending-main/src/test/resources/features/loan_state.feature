Feature: Loan Validation
  As a system
  I want to validate loan and funding rules
  So that invalid operations can be prevented

  Scenario: Unverified borrower is rejected when applying for a loan
    Given a borrower is registered but not verified
    When the borrower applies for a loan of 1000000
    Then the system should reject the loan application
    And an error message "Borrower not verified" should be shown

  Scenario: Loan application is rejected when amount is zero
    Given a borrower is registered and verified
    When the borrower applies for a loan of 0
    Then the system should reject the loan application
    And an error message "Invalid loan amount" should be shown

  Scenario: Investment is rejected when it would exceed the loan target amount
    Given there is a loan with target amount 1000000
    And the funded amount is 900000
    And a lender has sufficient balance of 200000
    When the lender invests 200000 into the loan
    Then the system should reject the investment
    And the funding error message "Exceeds loan target amount" should be shown

  Scenario: Investment is rejected when lender has insufficient balance
    Given there is a loan with target amount 1000000
    And a lender has balance of 100000
    When the lender invests 200000 into the loan
    Then the system should reject the investment
    And the funding error message "Insufficient balance" should be shown
