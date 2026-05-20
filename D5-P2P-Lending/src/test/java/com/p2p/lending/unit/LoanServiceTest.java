package com.p2p.lending.unit;

import com.p2p.lending.domain.model.Borrower;
import com.p2p.lending.domain.model.Lender;
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
 * Unit tests for Loan lifecycle orchestration (service-level scenarios).
 * Tests: apply → approve → startFunding → receiveFunding → fully funded → disburse
 *
 * @author UPI
 */
public class LoanServiceTest {

    private Borrower borrower;
    private Loan loan;

    public LoanServiceTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
        // Verified borrower via UserId constructor (konsisten LoanApplicationTest)
        borrower = new Borrower(new UserId("B001"));
        borrower.setVerified(true);

        // Loan di-create melalui domain method, bukan constructor langsung
        loan = borrower.applyForLoan(new Money(1_000_000));
    }

    @AfterEach
    public void tearDown() {
        borrower = null;
        loan = null;
    }

    // ========== SCENARIO 1: Loan lifecycle - apply ==========

    /**
     * Scenario: Loan created via applyForLoan has correct initial state
     *
     * Given a verified Borrower
     * When applyForLoan is called with 1000000 IDR
     * Then loan status should be APPLIED
     * And funded amount should be 0
     */
    @Test
    public void testLoanInitialState_afterApply() {
        assertEquals(LoanStatus.APPLIED, loan.getStatus(),
                "Status harus APPLIED setelah apply");
        assertEquals(new Money(0), loan.getFundedAmount(),
                "Funded amount harus 0 setelah apply");
        assertNotNull(loan.getLoanId(),
                "LoanId harus di-assign");
    }

    // ========== SCENARIO 2: Loan lifecycle - approve ==========

    /**
     * Scenario: Loan transitions from APPLIED to APPROVED
     *
     * Given a Loan in APPLIED status
     * When approve() is called
     * Then loan status should be APPROVED
     */
    @Test
    public void testLoanApproval_shouldTransitionToApproved() {
        // When
        loan.approve();

        // Then
        assertEquals(LoanStatus.APPROVED, loan.getStatus(),
                "Status harus APPROVED setelah approve");
    }

    // ========== SCENARIO 3: Cannot approve loan that is not APPLIED ==========

    /**
     * Scenario: Approving an already-approved loan throws exception
     *
     * Given a Loan already in APPROVED status
     * When approve() is called again
     * Then IllegalStateException should be thrown
     */
    @Test
    public void testApproveLoan_throwsException_whenNotApplied() {
        // Given
        loan.approve();

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            loan.approve();
        }, "Harus throw exception jika loan sudah APPROVED");
    }

    // ========== SCENARIO 4: Loan lifecycle - startFunding ==========

    /**
     * Scenario: Loan transitions from APPLIED to FUNDING
     *
     * Given a Loan in APPLIED status
     * When startFunding() is called
     * Then loan status should be FUNDING
     */
    @Test
    public void testStartFunding_shouldTransitionToFunding() {
        // When
        loan.startFunding();

        // Then
        assertEquals(LoanStatus.FUNDING, loan.getStatus(),
                "Status harus FUNDING setelah startFunding");
    }

    // ========== SCENARIO 5: Receive funding increases funded amount ==========

    /**
     * Scenario: receiveFunding correctly increases funded amount
     *
     * Given a Loan in FUNDING state
     * And a Lender with sufficient balance
     * When receiveFunding is called with 500000 IDR
     * Then funded amount should be 500000 IDR
     */
    @Test
    public void testReceiveFunding_shouldIncreaseFundedAmount() {
        // Given
        loan.startFunding();
        Lender lender = new Lender("L001", "Lina", "lina@email.com", 2_000_000);

        // When
        loan.receiveFunding(500_000, lender);

        // Then
        assertEquals(500_000, loan.getFundedAmountAsDouble(),
                "Funded amount harus 500000 setelah receive funding");
    }

    // ========== SCENARIO 6: Fully funded when target reached ==========

    /**
     * Scenario: Loan becomes FULLY_FUNDED when funded amount reaches target
     *
     * Given a Loan in FUNDING state with target 1000000 IDR
     * When receiveFunding is called with full amount 1000000 IDR
     * Then loan should be FULLY_FUNDED
     */
    @Test
    public void testLoan_shouldBeFullyFunded_whenTargetReached() {
        // Given
        loan.startFunding();
        Lender lender = new Lender("L001", "Lina", "lina@email.com", 2_000_000);

        // When
        loan.receiveFunding(1_000_000, lender);

        // Then
        assertTrue(loan.isFullyFunded(),
                "isFullyFunded() harus true");
        assertEquals(LoanStatus.FULLY_FUNDED, loan.getStatus(),
                "Status harus FULLY_FUNDED");
    }

    // ========== SCENARIO 7: Reject loan ==========

    /**
     * Scenario: Loan transitions from APPLIED to REJECTED
     *
     * Given a Loan in APPLIED status
     * When reject() is called
     * Then loan status should be REJECTED
     */
    @Test
    public void testRejectLoan_shouldTransitionToRejected() {
        // When
        loan.reject();

        // Then
        assertEquals(LoanStatus.REJECTED, loan.getStatus(),
                "Status harus REJECTED setelah reject");
    }

    // ========== SCENARIO 8: Cannot fund loan not in FUNDING state ==========

    /**
     * Scenario: receiveFunding throws exception if loan is not in FUNDING state
     *
     * Given a Loan in APPLIED status (not yet in FUNDING)
     * When receiveFunding is called
     * Then IllegalStateException should be thrown
     */
    @Test
    public void testReceiveFunding_throwsException_whenNotInFundingState() {
        // Given - loan masih APPLIED, belum startFunding
        Lender lender = new Lender("L001", "Lina", "lina@email.com", 2_000_000);

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            loan.receiveFunding(500_000, lender);
        }, "Harus throw exception jika loan bukan di state FUNDING");
    }
}