Feature: Loan State Transition
  As a P2P Lending system
  I want to ensure loan status transitions correctly
  So that the loan lifecycle integrity is maintained

  Scenario: Loan transitions from APPLIED to FUNDING
    Given a loan exists with status APPLIED
    When the system starts the funding process
    Then the loan status should change to FUNDING

  Scenario: Loan becomes FULLY FUNDED
    Given a loan exists with status FUNDING
    And the total collected funds have reached the target
    When the system checks the funding status
    Then the loan status should change to FULLY_FUNDED

  Scenario: Illegal transition is rejected by the system
    Given a loan exists with status APPLIED
    When the system tries to directly change status to DISBURSED
    Then the system should throw an error due to invalid transition