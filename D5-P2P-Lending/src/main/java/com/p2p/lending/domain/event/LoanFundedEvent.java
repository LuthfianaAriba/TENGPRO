package com.p2p.lending.domain.event;

public class LoanFundedEvent {
    private final String loanId;
    private final double amount;

    public LoanFundedEvent(String loanId, double amount) {
        this.loanId = loanId;
        this.amount = amount;
    }

    public String getLoanId() { return loanId; }
    public double getAmount() { return amount; }
}