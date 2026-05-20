package com.p2p.lending.unit;

import com.p2p.lending.domain.model.Borrower;
import com.p2p.lending.domain.model.Lender;
import com.p2p.lending.domain.model.Loan;
import com.p2p.lending.domain.observer.AdminNotificationObserver;
import com.p2p.lending.domain.observer.BorrowerNotificationObserver;
import com.p2p.lending.domain.observer.LenderNotificationObserver;
import com.p2p.lending.domain.observer.LoanFundingNotifier;
import com.p2p.lending.domain.observer.LoanFundingObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ObserverTest {

    private Loan loan;
    private Borrower borrower;

    @BeforeEach
    void setUp() {
        borrower = new Borrower("BR001", "Budi", "budi@email.com", 700);
        borrower.verify();
        loan = new Loan("LOAN-001", borrower, 1000000, "Modal Usaha", 12, 0.12);
        loan.approve();
        loan.startFunding();

        // Reset Singleton observer list setiap test
        // agar tidak saling pengaruh antar test
        LoanFundingNotifier.getInstance().clearObservers();
    }

    // ========== SCENARIO 1 ==========
    // Observer dapat notifikasi saat fully funded
    @Test
    void shouldNotifyObserver_whenLoanIsFullyFunded() {
        // Given
        boolean[] notified = {false};
        LoanFundingObserver testObserver = (l) -> notified[0] = true;
        LoanFundingNotifier.getInstance().addObserver(testObserver);

        Lender lender = new Lender("L001", "Lina", "lina@email.com", 1000000);

        // When
        loan.receiveFunding(1000000, lender);

        // Then
        assertTrue(loan.isFullyFunded());
        assertTrue(notified[0], "Observer harus dapat notifikasi saat fully funded");
    }

    // ========== SCENARIO 2 ==========
    // Observer TIDAK dapat notifikasi kalau belum fully funded
    @Test
    void shouldNotNotifyObserver_whenLoanIsNotFullyFunded() {
        // Given
        boolean[] notified = {false};
        LoanFundingObserver testObserver = (l) -> notified[0] = true;
        LoanFundingNotifier.getInstance().addObserver(testObserver);

        Lender lender = new Lender("L001", "Lina", "lina@email.com", 1000000);

        // When - hanya fund sebagian
        loan.receiveFunding(500000, lender);

        // Then
        assertFalse(loan.isFullyFunded());
        assertFalse(notified[0], "Observer tidak boleh dapat notifikasi kalau belum fully funded");
    }

    // ========== SCENARIO 3 ==========
    // Multiple observers semua dapat notifikasi
    @Test
    void shouldNotifyAllObservers_whenLoanIsFullyFunded() {
        // Given
        int[] notifyCount = {0};
        LoanFundingNotifier.getInstance().addObserver((l) -> notifyCount[0]++);
        LoanFundingNotifier.getInstance().addObserver((l) -> notifyCount[0]++);
        LoanFundingNotifier.getInstance().addObserver(new LenderNotificationObserver());
        LoanFundingNotifier.getInstance().addObserver(new BorrowerNotificationObserver());
        LoanFundingNotifier.getInstance().addObserver(new AdminNotificationObserver());

        Lender lender = new Lender("L001", "Lina", "lina@email.com", 1000000);

        // When
        loan.receiveFunding(1000000, lender);

        // Then
        assertEquals(2, notifyCount[0], "Kedua lambda observer harus dapat notifikasi");
    }

    // ========== SCENARIO 4 ==========
    // Observer yang di-remove tidak dapat notifikasi
    @Test
    void shouldNotNotifyRemovedObserver_whenLoanIsFullyFunded() {
        // Given
        boolean[] notified = {false};
        LoanFundingObserver testObserver = (l) -> notified[0] = true;
        LoanFundingNotifier.getInstance().addObserver(testObserver);
        LoanFundingNotifier.getInstance().removeObserver(testObserver);

        Lender lender = new Lender("L001", "Lina", "lina@email.com", 1000000);

        // When
        loan.receiveFunding(1000000, lender);

        // Then
        assertFalse(notified[0], "Observer yang di-remove tidak boleh dapat notifikasi");
    }
}