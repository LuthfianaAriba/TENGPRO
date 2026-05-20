package com.p2p.lending.steps;

import com.p2p.lending.domain.model.Borrower;
import com.p2p.lending.domain.model.Lender;
import com.p2p.lending.domain.model.Loan;
import com.p2p.lending.domain.valueobject.Money;
import com.p2p.lending.domain.valueobject.UserId;
import io.cucumber.java.en.*;
import static org.junit.jupiter.api.Assertions.*;

public class FundingStepDef {

    private Loan loan;
    private Lender lender;
    private Lender lenderA;
    private Lender lenderB;
    private Exception thrownException;

    @Given("there is a loan with status {string}")
    public void there_is_a_loan_with_status(String status) {
        Borrower borrower = new Borrower(new UserId("BR001"));
        borrower.setVerified(true);
        loan = borrower.applyForLoan(new Money(1_000_000));
        if (status.equals("FUNDING")) {
            loan.startFunding();
        }
    }

    @Given("there is a loan with target amount {long}")
    public void there_is_a_loan_with_target_amount(long targetAmount) {
        Borrower borrower = new Borrower(new UserId("BR001"));
        borrower.setVerified(true);
        loan = borrower.applyForLoan(new Money(targetAmount));
        loan.startFunding();
    }

    @Given("the funded amount is {long}")
    public void the_funded_amount_is(long amount) {
        Lender tempLender = new Lender("TEMP", "Temp", "temp@email.com", amount + 100_000);
        loan.receiveFunding(amount, tempLender);
    }

    @Given("a lender has sufficient balance")
    public void a_lender_has_sufficient_balance() {
        lender = new Lender("L001", "Lina", "lina@email.com", 2_000_000);
    }

    @Given("a lender has sufficient balance of {long}")
    public void a_lender_has_sufficient_balance_of(long balance) {
        lender = new Lender("L001", "Lina", "lina@email.com", balance);
    }

    @Given("a lender has balance of {long}")
    public void a_lender_has_balance_of(long balance) {
        lender = new Lender("L001", "Lina", "lina@email.com", balance);
    }

    @When("the loan receives funding of {long} from the lender")
    public void the_loan_receives_funding_of_from_the_lender(long amount) {
        try {
            loan.receiveFunding(amount, lender);
        } catch (Exception e) {
            thrownException = e;
        }
    }

    @When("the loan receives funding of {long} from lender A")
    public void the_loan_receives_funding_of_from_lender_a(long amount) {
        lenderA = new Lender("L001", "Lina", "lina@email.com", 2_000_000);
        loan.receiveFunding(amount, lenderA);
    }

    @When("the loan receives funding of {long} from lender B")
    public void the_loan_receives_funding_of_from_lender_b(long amount) {
        lenderB = new Lender("L002", "Bono", "bono@email.com", 2_000_000);
        loan.receiveFunding(amount, lenderB);
    }

    @When("the lender invests {long} into the loan")
    public void the_lender_invests_into_the_loan(long amount) {
        try {
            loan.receiveFunding(amount, lender);
        } catch (Exception e) {
            thrownException = e;
        }
    }

    @When("the system checks remaining funding needed")
    public void the_system_checks_remaining_funding_needed() {
    }

    @Then("the funded amount should be {long}")
    public void the_funded_amount_should_be(long expectedAmount) {
        assertEquals(expectedAmount, (long) loan.getFundedAmountAsDouble());
    }

    @Then("the loan should be fully funded")
    public void the_loan_should_be_fully_funded() {
        assertTrue(loan.isFullyFunded());
    }

    @Then("the remaining funding should be {long}")
    public void the_remaining_funding_should_be(long expectedRemaining) {
        assertEquals(expectedRemaining, (long) loan.getRemainingFundingNeeded());
    }

    @Then("the system should reject the funding")
    public void the_system_should_reject_the_funding() {
        assertNotNull(thrownException, "Exception harus dilempar");
    }

    @Then("the system should reject the investment")
    public void the_system_should_reject_the_investment() {
        assertNotNull(thrownException, "Exception harus dilempar");
    }

    @Then("the funding error message {string} should be shown")
    public void the_funding_error_message_should_be_shown(String expectedMessage) {
        assertNotNull(thrownException, "Exception harus ada");
        assertEquals(expectedMessage, thrownException.getMessage());
    }
}