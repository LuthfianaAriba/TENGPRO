package com.p2p.lending.domain.model;

public class Investment {

    private String id;
    private Lender lender;
    private String loanId;
    private double amount;

    public Investment(String id, Lender lender, String loanId, double amount) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Investment ID required");
        if (lender == null) throw new IllegalArgumentException("Lender required");
        if (loanId == null || loanId.isBlank()) throw new IllegalArgumentException("Loan ID required");
        if (amount <= 0) throw new IllegalArgumentException("Amount must be greater than 0");

        this.id = id;
        this.lender = lender;
        this.loanId = loanId;
        this.amount = amount;
    }

    public String getId() { return id; }
    public Lender getLender() { return lender; }
    public String getLoanId() { return loanId; }
    public double getAmount() { return amount; }
}