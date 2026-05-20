package com.p2p.lending.domain.specification;

import com.p2p.lending.domain.model.Borrower;
import com.p2p.lending.domain.model.Loan;

/**
 * Specification Pattern - MinimumCreditScoreSpecification
 *
 * Aturan bisnis: Loan hanya dapat diproses jika borrower
 * memiliki credit score minimal sesuai threshold yang ditentukan.
 *
 * Contoh threshold P2P Lending:
 *   - UMKM Loan     : minimal credit score 600
 *   - Consumer Loan : minimal credit score 650
 *
 * Threshold bisa dikonfigurasi saat membuat spesifikasi,
 * sehingga aturan ini reusable untuk berbagai jenis loan.
 */
public class MinimumCreditScoreSpecification implements LoanSpecification {

    private final int minimumScore;

    public MinimumCreditScoreSpecification(int minimumScore) {
        if (minimumScore < 0) throw new IllegalArgumentException("Minimum score tidak boleh negatif");
        this.minimumScore = minimumScore;
    }

    @Override
    public boolean isSatisfiedBy(Loan loan) {
        if (loan == null) return false;
        Borrower borrower = loan.getBorrower();
        if (borrower == null) return false;
        return borrower.getCreditScore() >= minimumScore;
    }

    public String getFailureReason(Loan loan) {
        if (loan == null || loan.getBorrower() == null) return "Borrower tidak ditemukan";
        int actual = loan.getBorrower().getCreditScore();
        return "Credit score " + actual + " di bawah minimum " + minimumScore;
    }

    public int getMinimumScore() {
        return minimumScore;
    }
}
