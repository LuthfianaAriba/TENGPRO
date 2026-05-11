package com.p2p.lending.steps;

/**
 *
 * @author febianaafra
 */
 import com.p2p.lending.domain.model.Loan;
import io.cucumber.java.en.*;
import static org.junit.jupiter.api.Assertions.*;

public class LoanStateStepDef {

    private Loan loan;
    private Exception exceptionThrown;

    @Given("a loan exists with status APPLIED")
    public void a_loan_exists_with_status_applied() {
        loan = new Loan("L001", 5_000_000);
    }

    @When("the system starts the funding process")
    public void the_system_starts_the_funding_process() {
        loan.startFunding();
    }

    @Then("the loan status should change to FUNDING")
    public void the_loan_status_should_change_to_funding() {
        assertEquals("FUNDING", loan.getStatus().name());
    }

    @Given("a loan exists with status FUNDING")
    public void a_loan_exists_with_status_funding() {
        loan = new Loan("L002", 5_000_000);
        loan.startFunding();
    }

    @And("the total collected funds have reached the target")
    public void the_total_collected_funds_have_reached_the_target() {
        loan.addFunding(5_000_000);
    }

    @When("the system checks the funding status")
    public void the_system_checks_the_funding_status() {
        loan.checkFundingCompletion();
    }

    @Then("the loan status should change to FULLY_FUNDED")
    public void the_loan_status_should_change_to_fully_funded() {
        assertEquals("FULLY_FUNDED", loan.getStatus().name());
    }

    @When("the system tries to directly change status to DISBURSED")
    public void the_system_tries_to_directly_change_status_to_disbursed() {
        try {
            loan.disburse();
        } catch (Exception e) {
            exceptionThrown = e;
        }
    }

    @Then("the system should throw an error due to invalid transition")
    public void the_system_should_throw_an_error_due_to_invalid_transition() {
        assertNotNull(exceptionThrown);
        assertTrue(exceptionThrown instanceof IllegalStateException);
    }
}