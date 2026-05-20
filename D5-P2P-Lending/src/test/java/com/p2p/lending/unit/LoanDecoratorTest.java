package com.p2p.lending.unit;

import com.p2p.lending.domain.decorator.CashbackDecorator;
import com.p2p.lending.domain.decorator.InsuranceDecorator;
import com.p2p.lending.domain.decorator.LoanComponent;
import com.p2p.lending.domain.decorator.PenaltyDecorator;
import com.p2p.lending.domain.decorator.SimpleLoan;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LoanDecoratorTest {

    // ========== SCENARIO 1 ==========
    // Simple loan tanpa decorator
    @Test
    void shouldReturnBaseCost_forSimpleLoan() {
        // Given
        LoanComponent loan = new SimpleLoan(1_000_000, "Personal Loan");

        // Then
        assertEquals(1_000_000, loan.getCost());
        assertTrue(loan.getDescription().contains("Base Cost"));
    }

    // ========== SCENARIO 2 ==========
    // Loan + Penalty
    @Test
    void shouldAddPenalty_whenLoanIsLate() {
        // Given
        LoanComponent loan = new SimpleLoan(1_000_000, "Personal Loan");

        // When
        LoanComponent loanWithPenalty = new PenaltyDecorator(loan, 500_000);

        // Then
        assertEquals(1_500_000, loanWithPenalty.getCost());
        assertTrue(loanWithPenalty.getDescription().contains("Penalty"));
    }

    // ========== SCENARIO 3 ==========
    // Loan + Insurance
    @Test
    void shouldAddInsuranceFee_whenInsuranceApplied() {
        // Given
        LoanComponent loan = new SimpleLoan(1_000_000, "Personal Loan");

        // When
        LoanComponent loanWithInsurance = new InsuranceDecorator(loan, 300_000);

        // Then
        assertEquals(1_300_000, loanWithInsurance.getCost());
        assertTrue(loanWithInsurance.getDescription().contains("Insurance"));
    }

    // ========== SCENARIO 4 ==========
    // Loan + Penalty + Insurance (stacking decorator)
    @Test
    void shouldStackDecorators_penaltyAndInsurance() {
        // Given
        LoanComponent loan = new SimpleLoan(1_000_000, "Personal Loan");

        // When - stack dua decorator
        LoanComponent loanWithPenalty = new PenaltyDecorator(loan, 500_000);
        LoanComponent loanWithPenaltyAndInsurance = new InsuranceDecorator(loanWithPenalty, 300_000);

        // Then
        assertEquals(1_800_000, loanWithPenaltyAndInsurance.getCost());
        assertTrue(loanWithPenaltyAndInsurance.getDescription().contains("Penalty"));
        assertTrue(loanWithPenaltyAndInsurance.getDescription().contains("Insurance"));
    }

    // ========== SCENARIO 5 ==========
    // Loan + Cashback (kurangi biaya)
    @Test
    void shouldReduceCost_whenCashbackApplied() {
        // Given
        LoanComponent loan = new SimpleLoan(1_000_000, "Personal Loan");

        // When
        LoanComponent loanWithCashback = new CashbackDecorator(loan, 100_000);

        // Then
        assertEquals(900_000, loanWithCashback.getCost());
        assertTrue(loanWithCashback.getDescription().contains("Cashback"));
    }

    // ========== SCENARIO 6 ==========
    // Loan + Penalty + Insurance + Cashback (full stacking)
    @Test
    void shouldCalculateCorrectly_withAllDecorators() {
        // Given - Base: 1.000.000
        LoanComponent loan = new SimpleLoan(1_000_000, "Personal Loan");

        // When
        LoanComponent withPenalty    = new PenaltyDecorator(loan, 500_000);    // +500.000
        LoanComponent withInsurance  = new InsuranceDecorator(withPenalty, 300_000); // +300.000
        LoanComponent withCashback   = new CashbackDecorator(withInsurance, 100_000); // -100.000

        // Then: 1.000.000 + 500.000 + 300.000 - 100.000 = 1.700.000
        assertEquals(1_700_000, withCashback.getCost());
    }
}