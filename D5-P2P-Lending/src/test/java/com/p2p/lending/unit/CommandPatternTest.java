package com.p2p.lending.unit;

import com.p2p.lending.domain.command.*;
import com.p2p.lending.domain.factory.LoanFactory;
import com.p2p.lending.domain.model.Borrower;
import com.p2p.lending.domain.model.Lender;
import com.p2p.lending.domain.model.Loan;
import com.p2p.lending.infrastructure.InMemoryLoanRepository;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD - Unit Test Command Pattern
 *
 * Fokus: menguji setiap command secara terisolasi,
 * termasuk LoanCommandInvoker sebagai invoker.
 */
@DisplayName("Command Pattern - Unit Test")
class CommandPatternTest {

    private InMemoryLoanRepository repo;
    private LoanCommandInvoker invoker;
    private Borrower borrower;
    private Lender lender;

    @BeforeEach
    void setUp() {
        repo     = new InMemoryLoanRepository();
        invoker  = new LoanCommandInvoker();
        borrower = new Borrower("B01", "Fajar", "fajar@mail.com", 680);
        borrower.verify();
        lender   = new Lender("L01", "Gita",  "gita@mail.com",  15_000_000);
    }

    // ----------------------------------------------------------
    // ApplyLoanCommand
    // ----------------------------------------------------------

    @Test
    @DisplayName("ApplyLoanCommand: getCreatedLoan() sebelum execute() harus throw")
    void applyLoanCommand_getCreatedLoanBeforeExecute_throws() {
        ApplyLoanCommand cmd = new ApplyLoanCommand(
                borrower, "L001", 5_000_000, 6, 0.10,
                LoanFactory.LoanType.UMKM, repo);

        assertThrows(IllegalStateException.class, cmd::getCreatedLoan);
    }

    @Test
    @DisplayName("ApplyLoanCommand: setelah execute() loan tersimpan di repository")
    void applyLoanCommand_afterExecute_loanSavedInRepo() {
        ApplyLoanCommand cmd = new ApplyLoanCommand(
                borrower, "L001", 5_000_000, 6, 0.10,
                LoanFactory.LoanType.CONSUMER, repo);
        invoker.invoke(cmd);

        assertTrue(repo.findById("L001").isPresent());
        assertEquals(5_000_000, cmd.getCreatedLoan().getTargetAmount(), 0.01);
    }

    @Test
    @DisplayName("ApplyLoanCommand: konstruksi dengan tenor <= 0 harus throw")
    void applyLoanCommand_zeroTenor_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                new ApplyLoanCommand(borrower, "L001", 5_000_000, 0, 0.10,
                        LoanFactory.LoanType.UMKM, repo));
    }

    // ----------------------------------------------------------
    // FundLoanCommand
    // ----------------------------------------------------------

    @Test
    @DisplayName("FundLoanCommand: loan tidak ditemukan → throw IllegalArgumentException")
    void fundLoanCommand_loanNotFound_throws() {
        FundLoanCommand cmd = new FundLoanCommand("NONEXISTENT", lender, 1_000_000, repo);
        assertThrows(IllegalArgumentException.class, () -> invoker.invoke(cmd));
    }

    @Test
    @DisplayName("FundLoanCommand: konstruksi dengan amount negatif harus throw")
    void fundLoanCommand_negativeAmount_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                new FundLoanCommand("L001", lender, -500, repo));
    }

    @Test
    @DisplayName("FundLoanCommand: pendanaan melebihi target harus throw")
    void fundLoanCommand_exceedsTarget_throws() {
        Loan loan = LoanFactory.createUmkmLoan("L001", borrower, 5_000_000, 6, 0.10);
        loan.startFunding();
        repo.save(loan);

        FundLoanCommand cmd = new FundLoanCommand("L001", lender, 10_000_000, repo); // melebihi target
        assertThrows(IllegalArgumentException.class, () -> invoker.invoke(cmd));
    }

    // ----------------------------------------------------------
    // DisburseLoanCommand
    // ----------------------------------------------------------

    @Test
    @DisplayName("DisburseLoanCommand: loan tidak ditemukan → throw IllegalArgumentException")
    void disburseLoanCommand_loanNotFound_throws() {
        DisburseLoanCommand cmd = new DisburseLoanCommand("GHOST", repo);
        assertThrows(IllegalArgumentException.class, () -> invoker.invoke(cmd));
    }

    @Test
    @DisplayName("DisburseLoanCommand: konstruksi dengan loanId null harus throw")
    void disburseLoanCommand_nullLoanId_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                new DisburseLoanCommand(null, repo));
    }

    // ----------------------------------------------------------
    // LoanCommandInvoker
    // ----------------------------------------------------------

    @Test
    @DisplayName("Invoker: clearHistory() mengosongkan history")
    void invoker_clearHistory_historyEmpty() {
        ApplyLoanCommand cmd = new ApplyLoanCommand(
                borrower, "L002", 3_000_000, 12, 0.12,
                LoanFactory.LoanType.UMKM, repo);
        invoker.invoke(cmd);
        assertEquals(1, invoker.getHistorySize());

        invoker.clearHistory();
        assertEquals(0, invoker.getHistorySize());
    }

    @Test
    @DisplayName("Invoker: multiple sukses dan gagal tercatat semua di history")
    void invoker_mixedCommands_allRecordedInHistory() {
        // Sukses
        invoker.invoke(new ApplyLoanCommand(
                borrower, "L003", 3_000_000, 12, 0.12,
                LoanFactory.LoanType.UMKM, repo));

        // Gagal — loan tidak ada
        assertThrows(Exception.class, () ->
                invoker.invoke(new DisburseLoanCommand("TIDAK_ADA", repo)));

        assertEquals(2, invoker.getHistorySize());
        assertTrue(invoker.getCommandHistory().get(0).startsWith("[SUCCESS]"));
        assertTrue(invoker.getCommandHistory().get(1).startsWith("[ERROR]"));
    }

    @Test
    @DisplayName("Invoker: getCommandHistory() tidak bisa dimodifikasi dari luar")
    void invoker_getCommandHistory_isImmutable() {
        invoker.invoke(new ApplyLoanCommand(
                borrower, "L004", 2_000_000, 6, 0.10,
                LoanFactory.LoanType.CONSUMER, repo));

        assertThrows(UnsupportedOperationException.class, () ->
                invoker.getCommandHistory().add("tampered"));
    }
}
