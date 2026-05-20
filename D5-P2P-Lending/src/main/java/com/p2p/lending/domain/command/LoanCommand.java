package com.p2p.lending.domain.command;

/**
 * Command Pattern - Command Interface
 *
 * Design Pattern: Command
 * Tujuan: Mengenkapsulasi setiap operasi loan (apply, fund, disburse)
 *         sebagai objek tersendiri sehingga:
 *         1. Operasi bisa di-queue / di-log
 *         2. Mendukung undo jika dibutuhkan
 *         3. Memisahkan "siapa yang minta" dari "siapa yang eksekusi"
 *
 * Relevansi ke domain P2P Lending:
 *   - ApplyLoanCommand   → pengajuan pinjaman oleh borrower
 *   - FundLoanCommand    → pendanaan oleh lender
 *   - DisburseLoanCommand → pencairan dana ke borrower
 */
public interface LoanCommand {

    /**
     * Eksekusi perintah. Implementasi harus melempar exception
     * jika precondition bisnis tidak terpenuhi.
     */
    void execute();

    /**
     * Nama perintah — dipakai untuk logging/audit trail.
     */
    String getCommandName();
}
