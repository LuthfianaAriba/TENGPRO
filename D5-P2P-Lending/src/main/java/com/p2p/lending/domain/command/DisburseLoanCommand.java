package com.p2p.lending.domain.command;

import com.p2p.lending.domain.model.Loan;
import com.p2p.lending.domain.repository.LoanRepository;

/**
 * Command Pattern - DisburseLoanCommand
 *
 * Mengenkapsulasi proses pencairan pinjaman ke borrower.
 *
 * Alur:
 *   1. Ambil Loan dari repository
 *   2. Delegasikan ke State Pattern via loan.disburse()
 *      — FullyFundedState yang memvalidasi boleh/tidak dicairkan
 *   3. Simpan Loan dengan status DISBURSED
 */
public class DisburseLoanCommand implements LoanCommand {

    private final String loanId;
    private final LoanRepository loanRepository;

    public DisburseLoanCommand(String loanId, LoanRepository loanRepository) {
        if (loanId == null || loanId.isBlank()) throw new IllegalArgumentException("Loan ID required");
        if (loanRepository == null) throw new IllegalArgumentException("LoanRepository required");

        this.loanId         = loanId;
        this.loanRepository = loanRepository;
    }

    @Override
    public void execute() {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan tidak ditemukan: " + loanId));

        // State Pattern — FullyFundedState memvalidasi pencairan
        loan.disburse();

        loanRepository.save(loan);
    }

    @Override
    public String getCommandName() {
        return "DisburseLoanCommand";
    }
}
