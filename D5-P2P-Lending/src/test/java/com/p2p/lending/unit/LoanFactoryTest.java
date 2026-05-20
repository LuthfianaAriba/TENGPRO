package com.p2p.lending.unit;

import com.p2p.lending.domain.factory.LoanFactory;
import com.p2p.lending.domain.model.Borrower;
import com.p2p.lending.domain.model.Loan;
import com.p2p.lending.domain.valueobject.LoanStatus;
import com.p2p.lending.domain.valueobject.UserId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoanFactoryTest {

    private Borrower verifiedBorrower;
    private Borrower unverifiedBorrower;

    @BeforeEach
    void setUp() {
        verifiedBorrower = new Borrower(new UserId("B001"));
        verifiedBorrower.setVerified(true);

        unverifiedBorrower = new Borrower(new UserId("B002"));
        unverifiedBorrower.setVerified(false);
    }

    // ========== SCENARIO 1 ==========
    // Factory berhasil buat UMKM Loan
    @Test
    void shouldCreateUmkmLoan_withFlatStrategy() {
        // When
        Loan loan = LoanFactory.createUmkmLoan(
                "LOAN-001", verifiedBorrower, 1_000_000, 12, 0.12);

        // Then
        assertNotNull(loan);
        assertEquals("LOAN-001", loan.getId());
        assertEquals(LoanStatus.APPLIED, loan.getStatus());
        assertEquals(1_000_000, loan.getTargetAmount());
        // Flat interest: 1000000 * 0.12 * (12/12) = 120000
        assertEquals(120000, loan.calculateMonthlyInstallment(), 0.01);
    }

    // ========== SCENARIO 2 ==========
    // Factory berhasil buat Consumer Loan
    @Test
    void shouldCreateConsumerLoan_withEffectiveStrategy() {
        // When
        Loan loan = LoanFactory.createConsumerLoan(
                "LOAN-002", verifiedBorrower, 1_000_000, 12, 0.12);

        // Then
        assertNotNull(loan);
        assertEquals("LOAN-002", loan.getId());
        assertEquals(LoanStatus.APPLIED, loan.getStatus());
        // Effective interest berbeda dari flat
        assertTrue(loan.calculateMonthlyInstallment() > 0);
    }

    // ========== SCENARIO 3 ==========
    // Factory pakai enum LoanType
    @Test
    void shouldCreateLoan_usingLoanTypeEnum() {
        // When
        Loan umkmLoan = LoanFactory.createLoan(
                LoanFactory.LoanType.UMKM, "LOAN-003",
                verifiedBorrower, 1_000_000, 12, 0.12);

        Loan consumerLoan = LoanFactory.createLoan(
                LoanFactory.LoanType.CONSUMER, "LOAN-004",
                verifiedBorrower, 1_000_000, 12, 0.12);

        // Then
        assertNotNull(umkmLoan);
        assertNotNull(consumerLoan);
    }

    // ========== SCENARIO 4 ==========
    // Factory reject borrower yang belum verified
    @Test
    void shouldThrowException_whenBorrowerNotVerified() {
        assertThrows(IllegalStateException.class, () -> {
            LoanFactory.createUmkmLoan(
                    "LOAN-005", unverifiedBorrower, 1_000_000, 12, 0.12);
        }, "Harus throw exception jika borrower belum verified");
    }

    // ========== SCENARIO 5 ==========
    // Factory reject amount negatif
    @Test
    void shouldThrowException_whenAmountIsZero() {
        assertThrows(IllegalArgumentException.class, () -> {
            LoanFactory.createUmkmLoan(
                    "LOAN-006", verifiedBorrower, 0, 12, 0.12);
        }, "Harus throw exception jika amount 0");
    }
}