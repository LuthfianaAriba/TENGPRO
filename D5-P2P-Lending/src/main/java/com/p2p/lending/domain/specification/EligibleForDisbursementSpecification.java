package com.p2p.lending.domain.specification;

import com.p2p.lending.domain.model.Loan;
import com.p2p.lending.domain.valueobject.LoanStatus;

/**
 * Specification Pattern - EligibleForDisbursementSpecification
 *
 * Aturan bisnis: Loan bisa dicairkan hanya jika:
 *   1. Statusnya FULLY_FUNDED (seluruh target terpenuhi)
 *   2. Dana yang terkumpul >= target amount
 *
 * Kedua kondisi harus terpenuhi untuk pencairan.
 */
public class EligibleForDisbursementSpecification implements LoanSpecification {

    @Override
    public boolean isSatisfiedBy(Loan loan) {
        if (loan == null) return false;
        return loan.getStatus() == LoanStatus.FULLY_FUNDED
                && loan.isFullyFunded();
    }

    public String getFailureReason(Loan loan) {
        if (loan == null) return "Loan tidak ditemukan";
        if (loan.getStatus() != LoanStatus.FULLY_FUNDED) {
            return "Loan berstatus " + loan.getStatus() + ", harus FULLY_FUNDED untuk dicairkan";
        }
        if (!loan.isFullyFunded()) {
            double remaining = loan.getRemainingFundingNeeded();
            return "Dana belum terpenuhi, masih kurang: " + remaining;
        }
        return "Tidak memenuhi syarat pencairan";
    }
}
