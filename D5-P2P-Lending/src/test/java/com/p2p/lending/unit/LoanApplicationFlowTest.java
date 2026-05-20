package com.p2p.lending.unit;

import com.p2p.lending.domain.command.*;
import com.p2p.lending.domain.factory.LoanFactory;
import com.p2p.lending.domain.model.Borrower;
import com.p2p.lending.domain.model.Lender;
import com.p2p.lending.domain.model.Loan;
import com.p2p.lending.domain.observer.*;
import com.p2p.lending.domain.specification.*;
import com.p2p.lending.domain.valueobject.LoanStatus;
import com.p2p.lending.infrastructure.InMemoryLoanRepository;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD - Simulasi Alur Lengkap: Pengajuan → Pendanaan → Pencairan
 *
 * Test ini menggantikan main class dan mensimulasikan
 * seluruh siklus hidup pinjaman di sistem P2P Lending.
 *
 * Pendekatan TDD:
 *   1. RED   — tulis test yang gagal terlebih dahulu
 *   2. GREEN — implementasi minimal agar test lulus
 *   3. REFACTOR — perbaiki kode tanpa ubah hasil test
 *
 * Design Pattern yang diuji:
 *   - Command Pattern  (ApplyLoanCommand, FundLoanCommand, DisburseLoanCommand)
 *   - Specification Pattern (EligibleForFunding, EligibleForDisbursement, MinCreditScore)
 *   - Factory Pattern  (LoanFactory)
 *   - State Pattern    (AppliedState → FundingState → FullyFundedState → DisbursedState)
 *   - Observer Pattern (LoanFundingNotifier)
 *   - Decorator Pattern (diuji di LoanDecoratorTest terpisah)
 *   - Strategy Pattern (FlatInterest, EffectiveInterest)
 */
@DisplayName("Simulasi Alur P2P Lending: Pengajuan hingga Pencairan")
class LoanApplicationFlowTest {

    // ========================================================
    // FIXTURES — dibuat ulang setiap test agar tidak ada shared state
    // ========================================================

    private InMemoryLoanRepository loanRepository;
    private LoanCommandInvoker invoker;
    private Borrower borrower;
    private Lender lender1;
    private Lender lender2;

    private static final String LOAN_ID     = "LOAN-TDD-001";
    private static final double LOAN_AMOUNT = 10_000_000;  // Rp 10 juta
    private static final int    TENOR       = 12;
    private static final double RATE        = 0.12;        // 12% per tahun

    @BeforeEach
    void setUp() {
        loanRepository = new InMemoryLoanRepository();
        invoker        = new LoanCommandInvoker();

        // Borrower sudah terverifikasi dengan credit score layak
        borrower = new Borrower("BR-001", "Andi Saputra", "andi@email.com", 700);
        borrower.verify();

        // Dua lender siap mendanai
        lender1 = new Lender("LND-001", "Budi Hartono", "budi@email.com", 8_000_000);
        lender2 = new Lender("LND-002", "Citra Dewi",   "citra@email.com", 5_000_000);

        // Bersihkan observer singleton agar test tidak saling mempengaruhi
        LoanFundingNotifier.getInstance().clearObservers();
    }

    // ========================================================
    // TAHAP 1 — PENGAJUAN PINJAMAN
    // ========================================================

    @Test
    @DisplayName("TC-01: Borrower terverifikasi dapat mengajukan pinjaman UMKM")
    void borrowerVerified_applyUmkmLoan_loanCreatedWithStatusApplied() {
        // ARRANGE
        ApplyLoanCommand cmd = new ApplyLoanCommand(
                borrower, LOAN_ID, LOAN_AMOUNT, TENOR, RATE,
                LoanFactory.LoanType.UMKM, loanRepository);

        // ACT
        invoker.invoke(cmd);
        Loan loan = cmd.getCreatedLoan();

        // ASSERT
        assertNotNull(loan, "Loan harus terbentuk setelah command dieksekusi");
        assertEquals(LOAN_ID,     loan.getId());
        assertEquals(LOAN_AMOUNT, loan.getTargetAmount(), 0.01);
        assertEquals(LoanStatus.APPLIED, loan.getStatus());
        assertEquals(0, loan.getFundedAmountAsDouble(), 0.01);

        // Pastikan tersimpan di repository
        assertTrue(loanRepository.findById(LOAN_ID).isPresent());
    }

    @Test
    @DisplayName("TC-02: Borrower tidak terverifikasi tidak bisa mengajukan pinjaman")
    void borrowerNotVerified_applyLoan_throwsException() {
        // ARRANGE
        Borrower unverifiedBorrower = new Borrower("BR-002", "Dedi", "dedi@email.com", 650);
        // sengaja TIDAK memanggil unverifiedBorrower.verify()

        // ACT & ASSERT
        assertThrows(IllegalStateException.class, () ->
            new ApplyLoanCommand(
                    unverifiedBorrower, "LOAN-X", LOAN_AMOUNT, TENOR, RATE,
                    LoanFactory.LoanType.UMKM, loanRepository).execute(),
            "Harus melempar IllegalStateException karena borrower belum diverifikasi"
        );
    }

    @Test
    @DisplayName("TC-03: Pengajuan dengan amount <= 0 harus ditolak")
    void applyLoan_zeroAmount_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
            new ApplyLoanCommand(borrower, LOAN_ID, 0, TENOR, RATE,
                    LoanFactory.LoanType.UMKM, loanRepository),
            "Amount 0 harus ditolak"
        );
    }

    @Test
    @DisplayName("TC-04: History command tercatat setelah pengajuan berhasil")
    void applyLoan_success_commandHistoryRecorded() {
        ApplyLoanCommand cmd = new ApplyLoanCommand(
                borrower, LOAN_ID, LOAN_AMOUNT, TENOR, RATE,
                LoanFactory.LoanType.UMKM, loanRepository);
        invoker.invoke(cmd);

        assertEquals(1, invoker.getHistorySize());
        assertTrue(invoker.getCommandHistory().get(0).contains("[SUCCESS]"));
        assertTrue(invoker.getCommandHistory().get(0).contains("ApplyLoanCommand"));
    }

    // ========================================================
    // TAHAP 2 — MEMBUKA PENDANAAN (startFunding)
    // ========================================================

    @Test
    @DisplayName("TC-05: Loan berstatus APPLIED dapat dibuka untuk pendanaan")
    void appliedLoan_startFunding_statusChangesToFunding() {
        // ARRANGE — buat loan terlebih dahulu
        Loan loan = LoanFactory.createUmkmLoan(LOAN_ID, borrower, LOAN_AMOUNT, TENOR, RATE);
        loanRepository.save(loan);

        // ACT
        loan.startFunding();
        loanRepository.save(loan);

        // ASSERT
        Loan saved = loanRepository.findById(LOAN_ID).orElseThrow();
        assertEquals(LoanStatus.FUNDING, saved.getStatus());
    }

    @Test
    @DisplayName("TC-06: Specification EligibleForFunding terpenuhi setelah startFunding()")
    void loan_afterStartFunding_satisfiesEligibleForFundingSpec() {
        Loan loan = LoanFactory.createUmkmLoan(LOAN_ID, borrower, LOAN_AMOUNT, TENOR, RATE);
        loan.startFunding();

        EligibleForFundingSpecification spec = new EligibleForFundingSpecification();
        assertTrue(spec.isSatisfiedBy(loan));
    }

    @Test
    @DisplayName("TC-07: Loan berstatus APPLIED tidak memenuhi EligibleForFunding")
    void appliedLoan_doesNotSatisfyEligibleForFundingSpec() {
        Loan loan = LoanFactory.createUmkmLoan(LOAN_ID, borrower, LOAN_AMOUNT, TENOR, RATE);
        // Tidak startFunding()

        EligibleForFundingSpecification spec = new EligibleForFundingSpecification();
        assertFalse(spec.isSatisfiedBy(loan));
    }

    // ========================================================
    // TAHAP 3 — PENDANAAN (FundLoanCommand)
    // ========================================================

    @Test
    @DisplayName("TC-08: Lender berhasil mendanai sebagian pinjaman")
    void lender_fundPartialAmount_fundedAmountIncreases() {
        // ARRANGE
        Loan loan = LoanFactory.createUmkmLoan(LOAN_ID, borrower, LOAN_AMOUNT, TENOR, RATE);
        loan.startFunding();
        loanRepository.save(loan);

        double fundAmount = 6_000_000;

        // ACT
        FundLoanCommand cmd = new FundLoanCommand(LOAN_ID, lender1, fundAmount, loanRepository);
        invoker.invoke(cmd);

        // ASSERT
        Loan updated = loanRepository.findById(LOAN_ID).orElseThrow();
        assertEquals(fundAmount, updated.getFundedAmountAsDouble(), 0.01);
        assertEquals(LoanStatus.FUNDING, updated.getStatus(), "Belum fully funded, status tetap FUNDING");
        assertEquals(LOAN_AMOUNT - fundAmount, updated.getRemainingFundingNeeded(), 0.01);
    }

    @Test
    @DisplayName("TC-09: Lender tidak bisa mendanai lebih dari saldo yang dimiliki")
    void lender_insufficientBalance_throwsException() {
        // Target loan = 10 juta, saldo lender1 = 8 juta
        // Gunakan amount 9 juta: di bawah target loan, tapi di atas saldo lender1
        // → validasi "Insufficient balance" yang akan terlempar
        Loan loan = LoanFactory.createUmkmLoan(LOAN_ID, borrower, LOAN_AMOUNT, TENOR, RATE);
        loan.startFunding();
        loanRepository.save(loan);

        double overBalance = 9_000_000; // > saldo lender1 (8 juta), tapi < target (10 juta)
        FundLoanCommand cmd = new FundLoanCommand(LOAN_ID, lender1, overBalance, loanRepository);

        assertThrows(IllegalStateException.class, () -> invoker.invoke(cmd),
                "Harus gagal dengan IllegalStateException karena saldo lender tidak cukup");
    }

    @Test
    @DisplayName("TC-10: Lender tidak bisa mendanai loan yang belum dibuka (APPLIED)")
    void lender_fundAppliedLoan_throwsException() {
        Loan loan = LoanFactory.createUmkmLoan(LOAN_ID, borrower, LOAN_AMOUNT, TENOR, RATE);
        // TIDAK startFunding()
        loanRepository.save(loan);

        FundLoanCommand cmd = new FundLoanCommand(LOAN_ID, lender1, 5_000_000, loanRepository);
        assertThrows(IllegalStateException.class, () -> invoker.invoke(cmd),
                "State APPLIED harus menolak receiveFunding()");
    }

    @Test
    @DisplayName("TC-11: Dua lender bisa mendanai satu loan secara bertahap")
    void twoLenders_fundLoanSequentially_totalFundedAmountCorrect() {
        Loan loan = LoanFactory.createUmkmLoan(LOAN_ID, borrower, LOAN_AMOUNT, TENOR, RATE);
        loan.startFunding();
        loanRepository.save(loan);

        invoker.invoke(new FundLoanCommand(LOAN_ID, lender1, 6_000_000, loanRepository));
        invoker.invoke(new FundLoanCommand(LOAN_ID, lender2, 4_000_000, loanRepository));

        Loan updated = loanRepository.findById(LOAN_ID).orElseThrow();
        assertEquals(10_000_000, updated.getFundedAmountAsDouble(), 0.01);
        assertEquals(LoanStatus.FULLY_FUNDED, updated.getStatus());
        assertEquals(2, updated.getInvestmentCount());
    }

    @Test
    @DisplayName("TC-12: Saldo lender berkurang setelah berinvestasi")
    void lender_afterFunding_balanceDecreases() {
        Loan loan = LoanFactory.createUmkmLoan(LOAN_ID, borrower, LOAN_AMOUNT, TENOR, RATE);
        loan.startFunding();
        loanRepository.save(loan);

        double fundAmount = 5_000_000;
        double balanceBefore = lender1.getBalance();

        invoker.invoke(new FundLoanCommand(LOAN_ID, lender1, fundAmount, loanRepository));

        assertEquals(balanceBefore - fundAmount, lender1.getBalance(), 0.01);
    }

    // ========================================================
    // TAHAP 4 — STATUS FULLY FUNDED & OBSERVER
    // ========================================================

    @Test
    @DisplayName("TC-13: Loan berstatus FULLY_FUNDED setelah seluruh target terpenuhi")
    void loan_fullyFunded_statusIsFullyFunded() {
        Loan loan = LoanFactory.createUmkmLoan(LOAN_ID, borrower, LOAN_AMOUNT, TENOR, RATE);
        loan.startFunding();
        loanRepository.save(loan);

        invoker.invoke(new FundLoanCommand(LOAN_ID, lender1, 6_000_000, loanRepository));
        invoker.invoke(new FundLoanCommand(LOAN_ID, lender2, 4_000_000, loanRepository));

        Loan updated = loanRepository.findById(LOAN_ID).orElseThrow();
        assertEquals(LoanStatus.FULLY_FUNDED, updated.getStatus());
        assertTrue(updated.isFullyFunded());
    }

    @Test
    @DisplayName("TC-14: Observer dipanggil saat loan fully funded")
    void loan_fullyFunded_observerNotified() {
        // ARRANGE — daftarkan observer
        StringBuilder notifLog = new StringBuilder();
        LoanFundingNotifier.getInstance().addObserver(l ->
                notifLog.append("NOTIFIED:").append(l.getId()));

        Loan loan = LoanFactory.createUmkmLoan(LOAN_ID, borrower, LOAN_AMOUNT, TENOR, RATE);
        loan.startFunding();
        loanRepository.save(loan);

        // ACT — danai hingga penuh
        invoker.invoke(new FundLoanCommand(LOAN_ID, lender1, 6_000_000, loanRepository));
        invoker.invoke(new FundLoanCommand(LOAN_ID, lender2, 4_000_000, loanRepository));

        // ASSERT
        assertTrue(notifLog.toString().contains("NOTIFIED:" + LOAN_ID),
                "Observer harus dipanggil saat loan fully funded");
    }

    @Test
    @DisplayName("TC-15: Specification EligibleForDisbursement terpenuhi setelah FULLY_FUNDED")
    void fullyFundedLoan_satisfiesEligibleForDisbursementSpec() {
        Loan loan = LoanFactory.createUmkmLoan(LOAN_ID, borrower, LOAN_AMOUNT, TENOR, RATE);
        loan.startFunding();
        loanRepository.save(loan);

        invoker.invoke(new FundLoanCommand(LOAN_ID, lender1, 6_000_000, loanRepository));
        invoker.invoke(new FundLoanCommand(LOAN_ID, lender2, 4_000_000, loanRepository));

        Loan updated = loanRepository.findById(LOAN_ID).orElseThrow();
        EligibleForDisbursementSpecification spec = new EligibleForDisbursementSpecification();
        assertTrue(spec.isSatisfiedBy(updated));
    }

    // ========================================================
    // TAHAP 5 — PENCAIRAN (DisburseLoanCommand)
    // ========================================================

    @Test
    @DisplayName("TC-16: Loan FULLY_FUNDED dapat dicairkan, status berubah DISBURSED")
    void fullyFundedLoan_disburse_statusIsDisbursed() {
        // ARRANGE
        Loan loan = LoanFactory.createUmkmLoan(LOAN_ID, borrower, LOAN_AMOUNT, TENOR, RATE);
        loan.startFunding();
        loanRepository.save(loan);
        invoker.invoke(new FundLoanCommand(LOAN_ID, lender1, 6_000_000, loanRepository));
        invoker.invoke(new FundLoanCommand(LOAN_ID, lender2, 4_000_000, loanRepository));

        // ACT
        invoker.invoke(new DisburseLoanCommand(LOAN_ID, loanRepository));

        // ASSERT
        Loan disbursed = loanRepository.findById(LOAN_ID).orElseThrow();
        assertEquals(LoanStatus.DISBURSED, disbursed.getStatus());
    }

    @Test
    @DisplayName("TC-17: Loan yang belum FULLY_FUNDED tidak bisa dicairkan")
    void partiallyFundedLoan_disburse_throwsException() {
        Loan loan = LoanFactory.createUmkmLoan(LOAN_ID, borrower, LOAN_AMOUNT, TENOR, RATE);
        loan.startFunding();
        loanRepository.save(loan);

        invoker.invoke(new FundLoanCommand(LOAN_ID, lender1, 6_000_000, loanRepository)); // belum penuh

        DisburseLoanCommand disburseCmd = new DisburseLoanCommand(LOAN_ID, loanRepository);
        assertThrows(IllegalStateException.class, () -> invoker.invoke(disburseCmd),
                "Loan belum fully funded tidak bisa dicairkan");
    }

    @Test
    @DisplayName("TC-18: Loan yang sudah DISBURSED tidak bisa dicairkan ulang")
    void disbursedLoan_disburseAgain_throwsException() {
        // Setup hingga disbursed
        Loan loan = LoanFactory.createUmkmLoan(LOAN_ID, borrower, LOAN_AMOUNT, TENOR, RATE);
        loan.startFunding();
        loanRepository.save(loan);
        invoker.invoke(new FundLoanCommand(LOAN_ID, lender1, 6_000_000, loanRepository));
        invoker.invoke(new FundLoanCommand(LOAN_ID, lender2, 4_000_000, loanRepository));
        invoker.invoke(new DisburseLoanCommand(LOAN_ID, loanRepository));

        // Coba disburse lagi
        assertThrows(IllegalStateException.class, () ->
            invoker.invoke(new DisburseLoanCommand(LOAN_ID, loanRepository)),
            "Loan yang sudah DISBURSED tidak bisa dicairkan kembali"
        );
    }

    @Test
    @DisplayName("TC-19: Loan yang sudah DISBURSED tidak bisa menerima pendanaan lagi")
    void disbursedLoan_receiveFunding_throwsException() {
        Loan loan = LoanFactory.createUmkmLoan(LOAN_ID, borrower, LOAN_AMOUNT, TENOR, RATE);
        loan.startFunding();
        loanRepository.save(loan);
        invoker.invoke(new FundLoanCommand(LOAN_ID, lender1, 6_000_000, loanRepository));
        invoker.invoke(new FundLoanCommand(LOAN_ID, lender2, 4_000_000, loanRepository));
        invoker.invoke(new DisburseLoanCommand(LOAN_ID, loanRepository));

        assertThrows(IllegalStateException.class, () ->
            invoker.invoke(new FundLoanCommand(LOAN_ID, lender1, 1_000_000, loanRepository)),
            "Loan DISBURSED tidak bisa menerima dana"
        );
    }

    // ========================================================
    // TAHAP 6 — ALUR PENUH (END-TO-END)
    // ========================================================

    @Test
    @DisplayName("TC-20: Alur penuh — pengajuan, pendanaan, pencairan berjalan sukses")
    void fullFlow_applyFundDisburse_allStepsSuccess() {
        // === LANGKAH 1: Pengajuan ===
        ApplyLoanCommand applyCmd = new ApplyLoanCommand(
                borrower, LOAN_ID, LOAN_AMOUNT, TENOR, RATE,
                LoanFactory.LoanType.UMKM, loanRepository);
        invoker.invoke(applyCmd);

        Loan loan = loanRepository.findById(LOAN_ID).orElseThrow();
        assertEquals(LoanStatus.APPLIED, loan.getStatus());

        // === LANGKAH 2: Buka pendanaan ===
        loan.startFunding();
        loanRepository.save(loan);
        assertEquals(LoanStatus.FUNDING, loanRepository.findById(LOAN_ID).orElseThrow().getStatus());

        // === LANGKAH 3: Pendanaan oleh dua lender ===
        invoker.invoke(new FundLoanCommand(LOAN_ID, lender1, 6_000_000, loanRepository));
        invoker.invoke(new FundLoanCommand(LOAN_ID, lender2, 4_000_000, loanRepository));

        Loan funded = loanRepository.findById(LOAN_ID).orElseThrow();
        assertEquals(LoanStatus.FULLY_FUNDED, funded.getStatus());
        assertEquals(10_000_000, funded.getFundedAmountAsDouble(), 0.01);

        // === LANGKAH 4: Pencairan ===
        invoker.invoke(new DisburseLoanCommand(LOAN_ID, loanRepository));

        Loan disbursed = loanRepository.findById(LOAN_ID).orElseThrow();
        assertEquals(LoanStatus.DISBURSED, disbursed.getStatus());

        // === VERIFIKASI COMMAND HISTORY ===
        // apply + 2 fund + disburse = 4 command
        assertEquals(4, invoker.getHistorySize());
        assertTrue(invoker.getCommandHistory().stream()
                .allMatch(h -> h.startsWith("[SUCCESS]")),
                "Semua command harus berhasil");
    }

    // ========================================================
    // TAHAP 7 — SPECIFICATION KOMBINASI (AND / NOT)
    // ========================================================

    @Test
    @DisplayName("TC-21: Specification kombinasi AND — kredit cukup DAN layak didanai")
    void combinedSpec_creditScoreAndEligibleForFunding_bothMustPass() {
        Loan loan = LoanFactory.createUmkmLoan(LOAN_ID, borrower, LOAN_AMOUNT, TENOR, RATE);
        loan.startFunding();

        LoanSpecification spec =
                new MinimumCreditScoreSpecification(600)
                .and(new EligibleForFundingSpecification());

        assertTrue(spec.isSatisfiedBy(loan),
                "Borrower credit score 700 >= 600 dan loan status FUNDING → harus terpenuhi");
    }

    @Test
    @DisplayName("TC-22: Specification NOT — loan yang tidak eligible for funding")
    void notSpec_appliedLoan_satisfiedByNotEligibleForFunding() {
        Loan loan = LoanFactory.createUmkmLoan(LOAN_ID, borrower, LOAN_AMOUNT, TENOR, RATE);
        // tidak startFunding()

        LoanSpecification notFundingSpec = new EligibleForFundingSpecification().not();
        assertTrue(notFundingSpec.isSatisfiedBy(loan),
                "Loan APPLIED bukan FUNDING, sehingga NOT(EligibleForFunding) = true");
    }

    @Test
    @DisplayName("TC-23: Credit score di bawah minimum → spesifikasi tidak terpenuhi")
    void lowCreditScore_doesNotSatisfyMinimumCreditScoreSpec() {
        Borrower lowScoreBorrower = new Borrower("BR-LOW", "Eko", "eko@email.com", 500);
        lowScoreBorrower.verify();
        Loan loan = LoanFactory.createUmkmLoan("LOAN-LOW", lowScoreBorrower, LOAN_AMOUNT, TENOR, RATE);

        MinimumCreditScoreSpecification spec = new MinimumCreditScoreSpecification(600);
        assertFalse(spec.isSatisfiedBy(loan));
        assertTrue(spec.getFailureReason(loan).contains("500"));
    }

    // ========================================================
    // TAHAP 8 — STRATEGY PATTERN (bunga)
    // ========================================================

    @Test
    @DisplayName("TC-24: UMKM Loan menggunakan Flat Interest Strategy")
    void umkmLoan_calculateInstallment_usesFlatInterest() {
        Loan loan = LoanFactory.createUmkmLoan(LOAN_ID, borrower, LOAN_AMOUNT, TENOR, RATE);
        // Flat: principal * rate * (tenor/12) = 10_000_000 * 0.12 * 1 = 1_200_000
        double expectedInterest = LOAN_AMOUNT * RATE * (TENOR / 12.0);
        assertEquals(expectedInterest, loan.calculateMonthlyInstallment(), 0.01);
    }

    @Test
    @DisplayName("TC-25: Consumer Loan menggunakan Effective Interest Strategy")
    void consumerLoan_calculateInstallment_usesEffectiveInterest() {
        Loan loan = LoanFactory.createConsumerLoan(LOAN_ID, borrower, LOAN_AMOUNT, TENOR, RATE);
        double interest = loan.calculateMonthlyInstallment();
        // Effective interest > 0 dan berbeda dari flat
        assertTrue(interest > 0, "Effective interest harus positif");

        double flatInterest = LOAN_AMOUNT * RATE * (TENOR / 12.0);
        assertNotEquals(flatInterest, interest, 0.01,
                "Effective interest harus berbeda dari flat interest");
    }

    // ========================================================
    // TAHAP 9 — COMMAND PATTERN: INVOKER ERROR HANDLING
    // ========================================================

    @Test
    @DisplayName("TC-26: Command gagal tetap tercatat di history dengan prefix ERROR")
    void failedCommand_recordedInHistoryWithErrorPrefix() {
        // Loan tidak ada di repository — command pasti gagal
        DisburseLoanCommand failCmd = new DisburseLoanCommand("LOAN-NOT-EXIST", loanRepository);

        assertThrows(IllegalArgumentException.class, () -> invoker.invoke(failCmd));

        assertEquals(1, invoker.getHistorySize());
        assertTrue(invoker.getCommandHistory().get(0).startsWith("[ERROR]"),
                "Command gagal harus masuk history dengan prefix [ERROR]");
    }

    @Test
    @DisplayName("TC-27: Invoker getCommandName() mengembalikan nama command yang benar")
    void commands_getCommandName_returnsCorrectName() {
        assertEquals("ApplyLoanCommand",
                new ApplyLoanCommand(borrower, LOAN_ID, LOAN_AMOUNT, TENOR, RATE,
                        LoanFactory.LoanType.UMKM, loanRepository).getCommandName());

        Loan dummy = LoanFactory.createUmkmLoan("X", borrower, LOAN_AMOUNT, TENOR, RATE);
        loanRepository.save(dummy);

        assertEquals("FundLoanCommand",
                new FundLoanCommand("X", lender1, 1000, loanRepository).getCommandName());
        assertEquals("DisburseLoanCommand",
                new DisburseLoanCommand("X", loanRepository).getCommandName());
    }
}
