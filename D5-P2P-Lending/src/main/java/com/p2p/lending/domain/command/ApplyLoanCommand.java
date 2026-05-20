package com.p2p.lending.domain.command;

import com.p2p.lending.domain.factory.LoanFactory;
import com.p2p.lending.domain.model.Borrower;
import com.p2p.lending.domain.model.Loan;
import com.p2p.lending.domain.repository.LoanRepository;

/**
 * Command Pattern - ApplyLoanCommand
 *
 * Mengenkapsulasi proses pengajuan pinjaman.
 * Borrower + detail pinjaman dimasukkan saat konstruksi,
 * eksekusi dipanggil terpisah sehingga command bisa di-queue.
 *
 * Alur:
 *   1. Validasi borrower terverifikasi (dilakukan oleh LoanFactory)
 *   2. Buat Loan via LoanFactory (Factory Pattern)
 *   3. Simpan ke LoanRepository
 *   4. Simpan referensi hasil untuk bisa di-query setelah execute()
 */
public class ApplyLoanCommand implements LoanCommand {

    private final Borrower borrower;
    private final String loanId;
    private final double amount;
    private final int tenorMonths;
    private final double interestRate;
    private final LoanFactory.LoanType loanType;
    private final LoanRepository loanRepository;

    // Hasil eksekusi — null sebelum execute() dipanggil
    private Loan createdLoan;

    public ApplyLoanCommand(Borrower borrower,
                            String loanId,
                            double amount,
                            int tenorMonths,
                            double interestRate,
                            LoanFactory.LoanType loanType,
                            LoanRepository loanRepository) {
        if (borrower == null)       throw new IllegalArgumentException("Borrower required");
        if (loanId == null || loanId.isBlank()) throw new IllegalArgumentException("Loan ID required");
        if (amount <= 0)            throw new IllegalArgumentException("Amount must be > 0");
        if (tenorMonths <= 0)       throw new IllegalArgumentException("Tenor must be > 0");
        if (loanRepository == null) throw new IllegalArgumentException("LoanRepository required");

        this.borrower      = borrower;
        this.loanId        = loanId;
        this.amount        = amount;
        this.tenorMonths   = tenorMonths;
        this.interestRate  = interestRate;
        this.loanType      = loanType;
        this.loanRepository = loanRepository;
    }

    @Override
    public void execute() {
        // Factory Pattern digunakan di sini untuk membuat Loan
        this.createdLoan = LoanFactory.createLoan(
                loanType, loanId, borrower, amount, tenorMonths, interestRate);

        loanRepository.save(createdLoan);
    }

    @Override
    public String getCommandName() {
        return "ApplyLoanCommand";
    }

    /**
     * Mengembalikan loan yang dibuat — hanya valid setelah execute()
     */
    public Loan getCreatedLoan() {
        if (createdLoan == null) {
            throw new IllegalStateException("Command belum dieksekusi. Panggil execute() terlebih dahulu.");
        }
        return createdLoan;
    }
}
