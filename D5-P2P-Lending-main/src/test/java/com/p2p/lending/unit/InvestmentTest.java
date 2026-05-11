package com.p2p.lending.unit;

import com.p2p.lending.domain.model.Borrower;
import com.p2p.lending.domain.model.Lender;
import com.p2p.lending.domain.model.Loan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InvestmentTest {

    private Borrower borrower;
    private Loan loan;

    @BeforeEach
    void setUp() {
        borrower = new Borrower("BR001", "Budi", "budi@email.com", 700);
        borrower.verify();
        loan = new Loan("LOAN-001", borrower, 1000000, "Modal Usaha", 12, 0.12);
        loan.approve();
        loan.startFunding();
    }

    // ========== SCENARIO 1 ==========
    @Test
    void shouldAddFundedAmount_whenLenderFundsLoan() {
        Lender lender = new Lender("L001", "Lina", "lina@email.com", 1000000);
        loan.receiveFunding(500000, lender);
        assertEquals(500000, loan.getFundedAmountAsDouble());
    }

    // ========== SCENARIO 2 ==========
    @Test
    void shouldBeFullyFunded_whenMultipleLendersFund() {
        Lender lenderA = new Lender("L001", "Lina", "lina@email.com", 1000000);
        Lender lenderB = new Lender("L002", "Bono", "bono@email.com", 1000000);
        loan.receiveFunding(400000, lenderA);
        loan.receiveFunding(600000, lenderB);
        assertEquals(1000000, loan.getFundedAmountAsDouble());
        assertTrue(loan.isFullyFunded());
    }

    // ========== SCENARIO 3 ==========
    @Test
    void shouldCalculateRemainingFunding_correctly() {
        Lender lender = new Lender("L001", "Lina", "lina@email.com", 1000000);
        loan.receiveFunding(300000, lender);
        double remaining = loan.getRemainingFundingNeeded();
        assertEquals(700000, remaining);
    }

    // ========== SCENARIO 4 ==========
    @Test
    void shouldReject_whenFundingExceedsTargetAmount() {
        Lender lender = new Lender("L001", "Lina", "lina@email.com", 1000000);
        loan.receiveFunding(900000, lender);
        Lender lender2 = new Lender("L002", "Bono", "bono@email.com", 1000000);
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> loan.receiveFunding(200000, lender2)
        );
        assertEquals("Exceeds loan target amount", exception.getMessage());
    }

    // ========== SCENARIO 5 ==========
    @Test
    void shouldReject_whenLenderHasInsufficientBalance() {
        Lender lender = new Lender("L001", "Lina", "lina@email.com", 100000);
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> loan.receiveFunding(500000, lender)
        );
        assertEquals("Insufficient balance", exception.getMessage());
    }
}