Feature: Loan Funding

  Scenario: Lender successfully funds a loan
    Given there is a loan with status "FUNDING"
    And a lender has sufficient balance
    When the loan receives funding of 500000 from the lender
    Then the funded amount should be 500000

  Scenario: Loan funded by multiple lenders
    Given there is a loan with target amount 1000000
    When the loan receives funding of 400000 from lender A
    And the loan receives funding of 600000 from lender B
    Then the funded amount should be 1000000
    And the loan should be fully funded

  Scenario: Remaining funding is calculated correctly
    Given there is a loan with target amount 1000000
    And the funded amount is 300000
    When the system checks remaining funding needed
    Then the remaining funding should be 700000

  Scenario: Funding cannot exceed loan target amount
    Given there is a loan with target amount 1000000
    And the funded amount is 900000
    And a lender has sufficient balance
    When the loan receives funding of 200000 from the lender
    Then the system should reject the funding
    And the funding error message "Exceeds loan target amount" should be shown

  Scenario: Lender cannot fund with insufficient balance
    Given there is a loan with status "FUNDING"
    And a lender has balance of 100000
    When the loan receives funding of 500000 from the lender
    Then the system should reject the funding
    And the funding error message "Insufficient balance" should be shown