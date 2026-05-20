package com.p2p.lending.domain.model;

public class Lender {

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @param balance the balance to set
     */
    public void setBalance(double balance) {
        this.balance = balance;
    }

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
        return getBalance() >= amount;
    }

    // Dipanggil oleh FundingState untuk kurangi saldo
    public void invest(double amount) {
        if (!canInvest(amount)) {
            throw new IllegalStateException("Insufficient balance");
        }
        this.setBalance(this.getBalance() - amount);
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