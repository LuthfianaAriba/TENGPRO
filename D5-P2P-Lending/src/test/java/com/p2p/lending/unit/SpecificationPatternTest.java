package com.p2p.lending.unit;

import com.p2p.lending.domain.factory.LoanFactory;
import com.p2p.lending.domain.model.Borrower;
import com.p2p.lending.domain.model.Lender;
import com.p2p.lending.domain.model.Loan;
import com.p2p.lending.domain.specification.*;
import com.p2p.lending.domain.valueobject.LoanStatus;
import com.p2p.lending.infrastructure.InMemoryLoanRepository;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD - Unit Test Specification Pattern
 *
 * Fokus: menguji setiap spesifikasi secara terisolasi,
 * termasuk kombinasi AND / OR / NOT.
 */
@DisplayName("Specification Pattern - Unit Test")
class SpecificationPatternTest {

    private Borrower borrower;

    @BeforeEach
    void setUp() {
        borrower = new Borrower("B01", "Hendra", "hendra@mail.com", 720);
        borrower.verify();
    }

    // ----------------------------------------------------------
    // EligibleForFundingSpecification
    // ----------------------------------------------------------

    @Test
    @DisplayName("EligibleForFunding: status FUNDING → terpenuhi")
    void eligibleForFunding_fundingStatus_satisfied() {
        Loan loan = LoanFactory.createUmkmLoan("L1", borrower, 5_000_000, 12, 0.12);
        loan.startFunding();
        assertTrue(new EligibleForFundingSpecification().isSatisfiedBy(loan));
    }

    @Test
    @DisplayName("EligibleForFunding: status APPLIED → tidak terpenuhi")
    void eligibleForFunding_appliedStatus_notSatisfied() {
        Loan loan = LoanFactory.createUmkmLoan("L1", borrower, 5_000_000, 12, 0.12);
        assertFalse(new EligibleForFundingSpecification().isSatisfiedBy(loan));
    }

    @Test
    @DisplayName("EligibleForFunding: null loan → false")
    void eligibleForFunding_nullLoan_returnsFalse() {
        assertFalse(new EligibleForFundingSpecification().isSatisfiedBy(null));
    }

    @Test
    @DisplayName("EligibleForFunding: getFailureReason() berisi info status saat ini")
    void eligibleForFunding_failureReason_containsCurrentStatus() {
        Loan loan = LoanFactory.createUmkmLoan("L1", borrower, 5_000_000, 12, 0.12);
        String reason = new EligibleForFundingSpecification().getFailureReason(loan);
        assertTrue(reason.contains("APPLIED"));
    }

    // ----------------------------------------------------------
    // EligibleForDisbursementSpecification
    // ----------------------------------------------------------

    @Test
    @DisplayName("EligibleForDisbursement: FULLY_FUNDED dan isFullyFunded → terpenuhi")
    void eligibleForDisbursement_fullyFunded_satisfied() {
        InMemoryLoanRepository repo = new InMemoryLoanRepository();
        Lender lender = new Lender("LND", "Ira", "ira@mail.com", 10_000_000);

        Loan loan = LoanFactory.createUmkmLoan("L1", borrower, 5_000_000, 12, 0.12);
        loan.startFunding();
        repo.save(loan);
        loan.receiveFunding(5_000_000, lender);

        assertTrue(new EligibleForDisbursementSpecification().isSatisfiedBy(loan));
    }

    @Test
    @DisplayName("EligibleForDisbursement: status FUNDING → tidak terpenuhi")
    void eligibleForDisbursement_fundingStatus_notSatisfied() {
        Loan loan = LoanFactory.createUmkmLoan("L1", borrower, 5_000_000, 12, 0.12);
        loan.startFunding();
        assertFalse(new EligibleForDisbursementSpecification().isSatisfiedBy(loan));
    }

    @Test
    @DisplayName("EligibleForDisbursement: null → false")
    void eligibleForDisbursement_null_returnsFalse() {
        assertFalse(new EligibleForDisbursementSpecification().isSatisfiedBy(null));
    }

    // ----------------------------------------------------------
    // MinimumCreditScoreSpecification
    // ----------------------------------------------------------

    @Test
    @DisplayName("MinCreditScore: score 720 dengan threshold 600 → terpenuhi")
    void minCreditScore_aboveThreshold_satisfied() {
        Loan loan = LoanFactory.createUmkmLoan("L1", borrower, 5_000_000, 12, 0.12);
        assertTrue(new MinimumCreditScoreSpecification(600).isSatisfiedBy(loan));
    }

    @Test
    @DisplayName("MinCreditScore: score sama dengan threshold → terpenuhi (batas bawah inklusif)")
    void minCreditScore_equalToThreshold_satisfied() {
        Loan loan = LoanFactory.createUmkmLoan("L1", borrower, 5_000_000, 12, 0.12);
        assertTrue(new MinimumCreditScoreSpecification(720).isSatisfiedBy(loan));
    }

    @Test
    @DisplayName("MinCreditScore: score 500 dengan threshold 600 → tidak terpenuhi")
    void minCreditScore_belowThreshold_notSatisfied() {
        Borrower lowScore = new Borrower("B02", "Joko", "joko@mail.com", 500);
        lowScore.verify();
        Loan loan = LoanFactory.createUmkmLoan("L2", lowScore, 5_000_000, 12, 0.12);

        assertFalse(new MinimumCreditScoreSpecification(600).isSatisfiedBy(loan));
    }

    @Test
    @DisplayName("MinCreditScore: threshold negatif → throw IllegalArgumentException")
    void minCreditScore_negativeThreshold_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                new MinimumCreditScoreSpecification(-1));
    }

    @Test
    @DisplayName("MinCreditScore: getFailureReason() berisi actual score dan minimum")
    void minCreditScore_failureReason_containsScores() {
        Borrower lowScore = new Borrower("B03", "Kiki", "kiki@mail.com", 500);
        lowScore.verify();
        Loan loan = LoanFactory.createUmkmLoan("L3", lowScore, 5_000_000, 12, 0.12);

        MinimumCreditScoreSpecification spec = new MinimumCreditScoreSpecification(600);
        String reason = spec.getFailureReason(loan);
        assertTrue(reason.contains("500"), "Harus berisi actual score");
        assertTrue(reason.contains("600"), "Harus berisi minimum score");
    }

    // ----------------------------------------------------------
    // Kombinasi Specification
    // ----------------------------------------------------------

    @Test
    @DisplayName("AND: credit cukup AND eligible for funding → keduanya harus true")
    void andCombination_bothTrue_satisfied() {
        Loan loan = LoanFactory.createUmkmLoan("L1", borrower, 5_000_000, 12, 0.12);
        loan.startFunding();

        LoanSpecification combined =
                new MinimumCreditScoreSpecification(600)
                .and(new EligibleForFundingSpecification());

        assertTrue(combined.isSatisfiedBy(loan));
    }

    @Test
    @DisplayName("AND: credit cukup AND eligible for funding — salah satu false → false")
    void andCombination_oneFalse_notSatisfied() {
        Loan loan = LoanFactory.createUmkmLoan("L1", borrower, 5_000_000, 12, 0.12);
        // tidak startFunding → EligibleForFunding = false

        LoanSpecification combined =
                new MinimumCreditScoreSpecification(600)
                .and(new EligibleForFundingSpecification());

        assertFalse(combined.isSatisfiedBy(loan));
    }

    @Test
    @DisplayName("OR: salah satu terpenuhi → OR terpenuhi")
    void orCombination_oneTrue_satisfied() {
        Loan loan = LoanFactory.createUmkmLoan("L1", borrower, 5_000_000, 12, 0.12);
        // APPLIED: EligibleForFunding false, MinCreditScore(600) true

        LoanSpecification combined =
                new EligibleForFundingSpecification()
                .or(new MinimumCreditScoreSpecification(600));

        assertTrue(combined.isSatisfiedBy(loan));
    }

    @Test
    @DisplayName("NOT: negasi EligibleForFunding pada loan APPLIED → true")
    void notCombination_appliedLoan_negationIsTrue() {
        Loan loan = LoanFactory.createUmkmLoan("L1", borrower, 5_000_000, 12, 0.12);
        LoanSpecification notFunding = new EligibleForFundingSpecification().not();
        assertTrue(notFunding.isSatisfiedBy(loan));
    }

    @Test
    @DisplayName("NOT: negasi EligibleForFunding pada loan FUNDING → false")
    void notCombination_fundingLoan_negationIsFalse() {
        Loan loan = LoanFactory.createUmkmLoan("L1", borrower, 5_000_000, 12, 0.12);
        loan.startFunding();

        LoanSpecification notFunding = new EligibleForFundingSpecification().not();
        assertFalse(notFunding.isSatisfiedBy(loan));
    }

    @Test
    @DisplayName("Rantai tiga spec: creditScore AND funding AND NOT disbursement")
    void chainedThreeSpecs_allConditionsMet_satisfied() {
        Loan loan = LoanFactory.createUmkmLoan("L1", borrower, 5_000_000, 12, 0.12);
        loan.startFunding();

        LoanSpecification spec =
                new MinimumCreditScoreSpecification(600)
                .and(new EligibleForFundingSpecification())
                .and(new EligibleForDisbursementSpecification().not());

        assertTrue(spec.isSatisfiedBy(loan),
                "Credit 720>=600, FUNDING, dan belum FULLY_FUNDED → semua terpenuhi");
    }
}
