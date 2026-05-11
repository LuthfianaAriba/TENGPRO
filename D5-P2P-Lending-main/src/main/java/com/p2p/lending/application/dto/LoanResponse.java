// LoanResponse.java
package com.p2p.lending.application.dto;

import com.p2p.lending.domain.valueobject.LoanStatus;

public class LoanResponse {
    private String loanId;
    private String borrowerId;
    private double targetAmount;
    private double fundedAmount;
    private LoanStatus status;

    public LoanResponse(String loanId, String borrowerId,
                        double targetAmount, double fundedAmount,
                        LoanStatus status) {
        this.loanId = loanId;
        this.borrowerId = borrowerId;
        this.targetAmount = targetAmount;
        this.fundedAmount = fundedAmount;
        this.status = status;
    }

    public String getLoanId()       { return loanId; }
    public String getBorrowerId()   { return borrowerId; }
    public double getTargetAmount() { return targetAmount; }
    public double getFundedAmount() { return fundedAmount; }
    public LoanStatus getStatus()   { return status; }
}