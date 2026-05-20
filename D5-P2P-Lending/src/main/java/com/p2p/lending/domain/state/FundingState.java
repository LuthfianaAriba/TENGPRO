package com.p2p.lending.domain.state;

import com.p2p.lending.domain.model.Investment;
import com.p2p.lending.domain.model.Lender;
import com.p2p.lending.domain.model.Loan;
import com.p2p.lending.domain.observer.LoanFundingNotifier;
import com.p2p.lending.domain.valueobject.LoanStatus;

public class FundingState implements LoanState {

    @Override
    public void startFunding(Loan loan) {
        throw new IllegalStateException("Loan is already in FUNDING state");
    }

    @Override
    public void completeFunding(Loan loan) {
        loan.setStatus(LoanStatus.FULLY_FUNDED);
        loan.setState(new FullyFundedState());
    }

    @Override
    public void disburse(Loan loan) {
        throw new IllegalStateException("Cannot disburse, loan is not fully funded yet");
    }

    @Override
    public void receiveFunding(Loan loan, double amount, Lender lender) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Invalid funding amount");
        }
        if (!lender.canInvest(amount)) {
            throw new IllegalStateException("Insufficient balance");
        }
        if (loan.getFundedAmountAsDouble() + amount > loan.getTargetAmount()) {
            throw new IllegalArgumentException("Exceeds loan target amount");
        }
        loan.addFundedAmount(amount);
        loan.addInvestment(new Investment(
                "INV-" + loan.getInvestmentCount(),
                lender,
                loan.getId(),
                amount
        ));
        lender.invest(amount);
        if (loan.getFundedAmountAsDouble() >= loan.getTargetAmount()) {
            completeFunding(loan);
            LoanFundingNotifier.getInstance().notifyFullyFunded(loan);
        }
    }
}