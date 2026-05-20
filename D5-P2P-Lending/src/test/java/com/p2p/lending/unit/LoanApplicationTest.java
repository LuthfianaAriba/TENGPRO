/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.p2p.lending.unit;

import com.p2p.lending.domain.event.LoanAppliedEvent;
import com.p2p.lending.domain.model.Borrower;
import com.p2p.lending.domain.model.Loan;
import com.p2p.lending.domain.valueobject.LoanStatus;
import com.p2p.lending.domain.valueobject.Money;
import com.p2p.lending.domain.valueobject.UserId;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author FIDELLA
 */
public class LoanApplicationTest {

    private Borrower verifiedBorrower;
    private Borrower unverifiedBorrower;

    public LoanApplicationTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
        // Setup verified borrower dengan UserId "B001"
        verifiedBorrower = new Borrower(new UserId("B001"));
        verifiedBorrower.setVerified(true);

        // Setup unverified borrower dengan UserId "B001"
        unverifiedBorrower = new Borrower(new UserId("B001"));
        unverifiedBorrower.setVerified(false);
    }

    @AfterEach
    public void tearDown() {
        verifiedBorrower = null;
        unverifiedBorrower = null;
    }

    /**
     * Scenario: Successful loan application by verified borrower
     *
     * Given a Borrower entity with UserId "B001" is registered and verified
     * When the Borrower applies for a loan with Money amount 1000000 IDR
     * Then a Loan Aggregate should be created with LoanId assigned
     * And the LoanStatus Value Object should be "APPLIED"
     * And the funded Money should be 0 IDR
     * And a LoanAppliedEvent should be published
     */
    @Test
    public void testSuccessfulLoanApplicationByVerifiedBorrower() {
        // Given
        Money loanAmount = new Money(1_000_000);

        // When
        Loan loan = verifiedBorrower.applyForLoan(loanAmount);

        // Then - Loan Aggregate harus dibuat dengan LoanId
        assertNotNull(loan, "Loan should be created");
        assertNotNull(loan.getLoanId(), "LoanId should be assigned");

        // LoanStatus harus "APPLIED"
        assertEquals(LoanStatus.APPLIED, loan.getStatus(),
                "Loan status should be APPLIED");

        // Funded money harus 0 IDR
        assertEquals(new Money(0), loan.getFundedAmount(),
                "Funded amount should be 0 IDR initially");

        // LoanAppliedEvent harus dipublikasikan
        assertFalse(loan.getDomainEvents().isEmpty(),
                "Domain events should not be empty");
        assertTrue(loan.getDomainEvents().stream()
                        .anyMatch(event -> event instanceof LoanAppliedEvent),
                "LoanAppliedEvent should be published");
    }

    /**
     * Scenario: Loan application fails due to invalid Money amount
     *
     * Given a Borrower entity with UserId "B001" is registered and verified
     * When the Borrower applies for a loan with Money amount -500000 IDR
     * Then the Loan Aggregate should reject the application
     * And an error message "Invalid loan amount" should be thrown from domain
     */
    @Test
    public void testLoanApplicationFailsWithNegativeAmount() {
        // Given
        Money invalidAmount = new Money(-500_000);

        // When & Then
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            verifiedBorrower.applyForLoan(invalidAmount);
        }, "Should throw exception for negative loan amount");

        assertEquals("Invalid loan amount", exception.getMessage(),
                "Error message should be 'Invalid loan amount'");
    }

    /**
     * Scenario: Loan application fails if Borrower is not verified
     *
     * Given a Borrower entity with UserId "B001" is registered but not verified
     * When the Borrower applies for a loan with Money amount 1000000 IDR
     * Then the Loan Aggregate should reject the application
     * And an error message "Borrower not verified" should be thrown from domain
     */
    @Test
    public void testLoanApplicationFailsIfBorrowerNotVerified() {
        // Given
        Money loanAmount = new Money(1_000_000);

        // When & Then
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            unverifiedBorrower.applyForLoan(loanAmount);
        }, "Should throw exception for unverified borrower");

        assertEquals("Borrower not verified", exception.getMessage(),
                "Error message should be 'Borrower not verified'");
    }
}