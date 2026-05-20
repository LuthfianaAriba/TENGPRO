package com.p2p.lending.domain.specification;

import com.p2p.lending.domain.model.Loan;
import com.p2p.lending.domain.valueobject.LoanStatus;

/**
 * Specification Pattern - EligibleForFundingSpecification
 *
 * Aturan bisnis: Loan bisa menerima pendanaan hanya jika statusnya FUNDING.
 *
 * Catatan: Loan harus sudah di-startFunding() sebelum bisa menerima
 * investasi dari lender. Status APPLIED belum terbuka untuk pendanaan.
 */
public class EligibleForFundingSpecification implements LoanSpecification {

    @Override
    public boolean isSatisfiedBy(Loan loan) {
        if (loan == null) return false;
        return loan.getStatus() == LoanStatus.FUNDING;
    }

    /**
     * Pesan penjelasan jika spesifikasi tidak terpenuhi — berguna untuk
     * menampilkan error yang informatif ke user.
     */
    public String getFailureReason(Loan loan) {
        if (loan == null) return "Loan tidak ditemukan";
        return "Loan berstatus " + loan.getStatus() + ", harus FUNDING untuk bisa didanai";
    }
}
