// LoanRequest.java
package com.p2p.lending.application.dto;

public class LoanRequest {
    private String borrowerId;
    private double amount;
    private String purpose;
    private int tenorMonths;
    private double interestRate;

    public LoanRequest(String borrowerId, double amount, String purpose,
                       int tenorMonths, double interestRate) {
        this.borrowerId = borrowerId;
        this.amount = amount;
        this.purpose = purpose;
        this.tenorMonths = tenorMonths;
        this.interestRate = interestRate;
    }

    public String getBorrowerId()  { return borrowerId; }
    public double getAmount()      { return amount; }
    public String getPurpose()     { return purpose; }
    public int getTenorMonths()    { return tenorMonths; }
    public double getInterestRate(){ return interestRate; }
}