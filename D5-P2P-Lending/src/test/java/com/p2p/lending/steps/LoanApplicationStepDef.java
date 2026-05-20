package com.p2p.lending.steps;

import com.p2p.lending.domain.model.Borrower;
import com.p2p.lending.domain.model.Loan;
import com.p2p.lending.domain.valueobject.Money;
import com.p2p.lending.domain.valueobject.UserId;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import static org.junit.jupiter.api.Assertions.*;

public class LoanApplicationStepDef {

    private Borrower borrower;
    private Loan loan;
    private Exception thrownException;

    @Given("a Borrower entity with UserId {string} is registered and verified")
    public void aBorrowerIsRegisteredAndVerified(String userId) {
        borrower = new Borrower(new UserId(userId));
        borrower.setVerified(true);
    }

    @Given("a Borrower entity with UserId {string} is registered but not verified")
    public void aBorrowerIsRegisteredButNotVerified(String userId) {
        borrower = new Borrower(new UserId(userId));
        borrower.setVerified(false);
    }

    @When("the Borrower applies for a loan with Money amount {long} IDR")
    public void theBorrowerAppliesForLoan(long amount) {
        try {
            loan = borrower.applyForLoan(new Money(amount));
        } catch (Exception e) {
            thrownException = e;
        }
    }

    @Then("a Loan Aggregate should be created with LoanId assigned")
    public void aLoanShouldBeCreated() {
        assertNotNull(loan, "Loan should be created");
        assertNotNull(loan.getLoanId(), "LoanId should be assigned");
    }

    @Then("the LoanStatus Value Object should be {string}")
    public void theLoanStatusValueObjectShouldBe(String expectedStatus) {
        assertEquals(expectedStatus, loan.getStatus().name(),
                "Loan status should be " + expectedStatus);
    }

    @Then("the funded Money should be {long} IDR")
    public void theFundedMoneyShouldBe(long expectedAmount) {
        assertEquals(new Money(expectedAmount), loan.getFundedAmount(),
                "Funded amount should be " + expectedAmount + " IDR");
    }

    @Then("a LoanAppliedEvent should be published")
    public void aLoanAppliedEventShouldBePublished() {
        assertFalse(loan.getDomainEvents().isEmpty(),
                "Domain events should not be empty");
    }

    @Then("the loan application should be rejected with message {string}")
    public void theLoanApplicationShouldBeRejected(String expectedMessage) {
        assertNotNull(thrownException, "Exception should be thrown");
        assertEquals(expectedMessage, thrownException.getMessage());
    }
}