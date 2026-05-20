package com.p2p.lending.domain.state;

import com.p2p.lending.domain.model.Loan;
import com.p2p.lending.domain.valueobject.LoanStatus;
import com.p2p.lending.domain.model.Lender;

public class FullyFundedState implements LoanState {

    @Override
    public void startFunding(Loan loan) {
        throw new IllegalStateException("Cannot start funding, loan is already fully funded");
    }

    @Override
    public void completeFunding(Loan loan) {
        throw new IllegalStateException("Loan is already fully funded");
    }

    @Override
    public void disburse(Loan loan) {
        loan.setStatus(LoanStatus.DISBURSED);
        loan.setState(new DisbursedState());
    }
    
    @Override
    public void receiveFunding(Loan loan, double amount, Lender lender) {
        throw new IllegalStateException("Cannot receive funding in " + this.getClass().getSimpleName());
}
}