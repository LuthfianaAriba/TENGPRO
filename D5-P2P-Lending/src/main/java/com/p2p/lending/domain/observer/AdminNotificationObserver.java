package com.p2p.lending.domain.observer;

import com.p2p.lending.domain.model.Loan;

public class AdminNotificationObserver implements LoanFundingObserver {

    @Override
    public void onLoanFullyFunded(Loan loan) {
        System.out.println("[NOTIFIKASI ADMIN] Loan " 
            + loan.getLoanId() 
            + " fully funded. Mohon proses pencairan dana.");
    }
}