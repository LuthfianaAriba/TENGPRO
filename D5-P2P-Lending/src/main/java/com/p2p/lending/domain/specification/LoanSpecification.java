package com.p2p.lending.domain.specification;

import com.p2p.lending.domain.model.Loan;

/**
 * Specification Pattern - LoanSpecification
 *
 * Design Pattern: Specification
 * Tujuan: Mengenkapsulasi aturan bisnis terkait Loan sebagai objek
 *         yang bisa dikombinasikan (AND, OR, NOT) tanpa mengubah domain model.
 *
 * Keunggulan vs. if-else di domain model:
 *   - Setiap aturan bisnis tersendiri → mudah diuji unit
 *   - Kombinasi aturan tanpa duplikasi kode
 *   - Open/Closed Principle: tambah aturan baru tanpa ubah yang lama
 *
 * Contoh penggunaan di P2P Lending:
 *   - EligibleForFundingSpecification   : cek loan layak didanai
 *   - EligibleForDisbursementSpecification : cek loan siap dicairkan
 *   - MinimumCreditScoreSpecification   : cek credit score borrower
 */
public interface LoanSpecification {

    /**
     * Evaluasi apakah loan memenuhi spesifikasi ini.
     *
     * @param loan objek Loan yang dievaluasi
     * @return true jika terpenuhi
     */
    boolean isSatisfiedBy(Loan loan);

    /**
     * Gabungkan dengan spesifikasi lain menggunakan AND.
     */
    default LoanSpecification and(LoanSpecification other) {
        return loan -> this.isSatisfiedBy(loan) && other.isSatisfiedBy(loan);
    }

    /**
     * Gabungkan dengan spesifikasi lain menggunakan OR.
     */
    default LoanSpecification or(LoanSpecification other) {
        return loan -> this.isSatisfiedBy(loan) || other.isSatisfiedBy(loan);
    }

    /**
     * Negasi spesifikasi ini.
     */
    default LoanSpecification not() {
        return loan -> !this.isSatisfiedBy(loan);
    }
}
