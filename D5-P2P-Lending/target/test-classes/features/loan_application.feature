Feature: Loan Application

  Scenario: Successful loan application
    Given a borrower is registered and verified
    When the borrower applies for a loan of 1000000
    Then the loan should be created
    And the loan status should be "APPLIED"

  Scenario: Loan application fails due to invalid amount
    Given a borrower is registered and verified
    When the borrower applies for a loan of -500000
    Then the system should reject the loan application
    And an error message "Invalid loan amount" should be shown