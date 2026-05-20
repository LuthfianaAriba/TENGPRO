/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.p2p.lending.domain.state;

import com.p2p.lending.domain.state.LoanState;
import com.p2p.lending.domain.valueobject.LoanStatus;
import com.p2p.lending.domain.model.Loan;
import com.p2p.lending.domain.model.Lender;

/**
 *
 * @author febianaafra
 */
public class AppliedState implements LoanState {

    @Override
    public void startFunding(Loan loan) {
        loan.setState(new FundingState());
        loan.setStatus(LoanStatus.FUNDING);
    }

    @Override
    public void completeFunding(Loan loan) {
        throw new IllegalStateException("Cannot complete funding from APPLIED status");
    }

    @Override
    public void disburse(Loan loan) {
        throw new IllegalStateException("Cannot disburse from APPLIED status");
    }
    
    @Override
    public void receiveFunding(Loan loan, double amount, Lender lender) {
        throw new IllegalStateException("Cannot receive funding in " + this.getClass().getSimpleName());
}
}