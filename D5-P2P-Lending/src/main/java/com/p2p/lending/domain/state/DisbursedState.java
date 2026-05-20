package com.p2p.lending.domain.state;

import com.p2p.lending.domain.model.Loan;
import com.p2p.lending.domain.model.Lender;

public class DisbursedState implements LoanState {

    @Override
    public void startFunding(Loan loan) {
        throw new IllegalStateException("Cannot start funding, loan is already disbursed");
    }

    @Override
    public void completeFunding(Loan loan) {
        throw new IllegalStateException("Cannot complete funding, loan is already disbursed");
    }

    @Override
    public void disburse(Loan loan) {
        throw new IllegalStateException("Loan is already disbursed");
    }
    
    @Override
    public void receiveFunding(Loan loan, double amount, Lender lender) {
        throw new IllegalStateException("Cannot receive funding in " + this.getClass().getSimpleName());
}
}