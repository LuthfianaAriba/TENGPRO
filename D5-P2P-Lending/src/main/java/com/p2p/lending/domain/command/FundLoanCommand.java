package com.p2p.lending.domain.command;

import com.p2p.lending.domain.model.Lender;
import com.p2p.lending.domain.model.Loan;
import com.p2p.lending.domain.repository.LoanRepository;

/**
 * Command Pattern - FundLoanCommand
 *
 * Mengenkapsulasi proses pendanaan pinjaman oleh lender.
 * Satu FundLoanCommand mewakili satu transaksi investasi.
 *
 * Alur:
 *   1. Ambil Loan dari repository (validasi ada/tidak)
 *   2. Delegasikan ke State Pattern via loan.receiveFunding()
 *      — FundingState yang menangani validasi saldo, batas amount, dll.
 *   3. Simpan Loan yang sudah diperbarui
 */
public class FundLoanCommand implements LoanCommand {

    private final String loanId;
    private final Lender lender;
    private final double amount;
    private final LoanRepository loanRepository;

    public FundLoanCommand(String loanId,
                           Lender lender,
                           double amount,
                           LoanRepository loanRepository) {
        if (loanId == null || loanId.isBlank()) throw new IllegalArgumentException("Loan ID required");
        if (lender == null)    throw new IllegalArgumentException("Lender required");
        if (amount <= 0)       throw new IllegalArgumentException("Amount must be > 0");
        if (loanRepository == null) throw new IllegalArgumentException("LoanRepository required");

        this.loanId         = loanId;
        this.lender         = lender;
        this.amount         = amount;
        this.loanRepository = loanRepository;
    }

    @Override
    public void execute() {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan tidak ditemukan: " + loanId));

        // State Pattern — FundingState yang memvalidasi & memproses
        loan.receiveFunding(amount, lender);

        loanRepository.save(loan);
    }

    @Override
    public String getCommandName() {
        return "FundLoanCommand";
    }
}
