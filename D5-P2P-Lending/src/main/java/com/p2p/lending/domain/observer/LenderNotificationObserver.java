package com.p2p.lending.domain.observer;

import com.p2p.lending.domain.model.Loan;

public class LenderNotificationObserver implements LoanFundingObserver {

    @Override
    public void onLoanFullyFunded(Loan loan) {
        System.out.println("[NOTIFIKASI LENDER] Loan " 
            + loan.getLoanId() 
            + " sudah fully funded! Total dana: " 
            + loan.getFundedAmountAsDouble() + " IDR");
    }
}