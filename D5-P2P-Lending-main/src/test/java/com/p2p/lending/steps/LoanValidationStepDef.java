package com.p2p.lending.steps;

import com.p2p.lending.domain.model.Borrower;
import com.p2p.lending.domain.model.Loan;
import com.p2p.lending.domain.valueobject.Money;
import com.p2p.lending.domain.valueobject.UserId;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import static org.junit.jupiter.api.Assertions.*;

public class LoanValidationStepDef {

    private Borrower borrower;
    private Loan loan;
    private Exception thrownException;

    @Given("a borrower is registered but not verified")
    public void aBorrowerIsRegisteredButNotVerified() {
        borrower = new Borrower(new UserId("B001"));
        borrower.setVerified(false);
    }

    @Given("a borrower is registered and verified")
    public void aBorrowerIsRegisteredAndVerified() {
        borrower = new Borrower(new UserId("B001"));
        borrower.setVerified(true);
    }

    @When("the borrower applies for a loan of {int}")
    public void theBorrowerAppliesForALoanOf(int amount) {
        try {
            loan = borrower.applyForLoan(new Money(amount));
        } catch (Exception e) {
            thrownException = e;
        }
    }

    @Then("the loan should be created")
    public void theLoanShouldBeCreated() {
        assertNotNull(loan, "Loan should be created");
        assertNotNull(loan.getLoanId(), "LoanId should be assigned");
    }

    @Then("the loan status should be {string}")
    public void theLoanStatusShouldBe(String expectedStatus) {
        assertNotNull(loan, "Loan should exist");
        assertEquals(expectedStatus, loan.getStatus().name());
    }

    @Then("the system should reject the loan application")
    public void theSystemShouldRejectTheLoanApplication() {
        assertNotNull(thrownException,
                "Exception harus di-throw saat loan ditolak");
    }

    @Then("an error message {string} should be shown")
    public void anErrorMessageShouldBeShown(String expectedMessage) {
        assertNotNull(thrownException, "Exception harus ada");
        assertEquals(expectedMessage, thrownException.getMessage());
    }
}