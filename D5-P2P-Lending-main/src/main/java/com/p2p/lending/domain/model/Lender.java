package com.p2p.lending.domain.model;

public class Lender {

    private String id;
    private String name;
    private String email;
    private double balance;

    public Lender(String id, String name, String email, double balance) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Lender ID required");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name required");
        if (email == null || !email.contains("@")) throw new IllegalArgumentException("Invalid email");
        if (balance < 0) throw new IllegalArgumentException("Balance cannot be negative");

        this.id = id;
        this.name = name;
        this.email = email;
        this.balance = balance;
    }

    // Dipanggil oleh FundingState untuk cek saldo
    public boolean canInvest(double amount) {
        return balance >= amount;
    }

    // Dipanggil oleh FundingState untuk kurangi saldo
    public void invest(double amount) {
        if (!canInvest(amount)) {
            throw new IllegalStateException("Insufficient balance");
        }
        this.balance -= amount;
    }

    // Alias untuk backward compatibility
    public void deductBalance(double amount) {
        invest(amount);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public double getBalance() { return balance; }
}