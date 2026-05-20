package com.p2p.lending.domain.event;
import com.p2p.lending.domain.valueobject.Money;

public class LoanAppliedEvent {
    private final String loanId;
    private final Money amount;

    public LoanAppliedEvent(String loanId, Money amount) {
        this.loanId = loanId;
        this.amount = amount;
    }

    public String getLoanId() {
        return loanId;
    }

    public Money getAmount() {
        return amount;
    }
}