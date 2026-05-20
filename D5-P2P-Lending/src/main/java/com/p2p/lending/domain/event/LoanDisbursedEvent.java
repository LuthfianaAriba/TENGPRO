package com.p2p.lending.domain.event;

public class LoanDisbursedEvent {
    private final String loanId;

    public LoanDisbursedEvent(String loanId) {
        this.loanId = loanId;
    }

    public String getLoanId() { return loanId; }
}