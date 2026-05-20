package com.p2p.lending.domain.state;

import com.p2p.lending.domain.model.Lender;
import com.p2p.lending.domain.model.Loan;

public interface LoanState {
    void startFunding(Loan loan);
    void completeFunding(Loan loan);
    void disburse(Loan loan);
    void receiveFunding(Loan loan, double amount, Lender lender);
}