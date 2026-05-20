package com.p2p.lending.domain.model;

import com.p2p.lending.domain.valueobject.UserId;

public abstract class User {

    protected UserId userId;
    protected String name;
    protected String email;
    protected boolean isVerified;

    // Constructor lengkap — dipakai Borrower(String id, name, email, ...)
    protected User(String id, String name, String email) {
        if (id == null || id.isBlank())
            throw new IllegalArgumentException("User ID required");
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Name required");
        if (email == null || !email.contains("@"))
            throw new IllegalArgumentException("Invalid email");
        this.userId = new UserId(id);
        this.name = name;
        this.email = email;
        this.isVerified = false;
    }

    // Constructor minimal — dipakai Borrower(UserId userId)
    protected User(UserId userId) {
        if (userId == null)
            throw new IllegalArgumentException("UserId required");
        this.userId = userId;
        this.isVerified = false;
    }

    // Verify methods — sama persis seperti yang sudah ada di Borrower
    public void verify() {
        this.isVerified = true;
    }

    public void setVerified(boolean verified) {
        this.isVerified = verified;
    }

    public boolean isVerified() {
        return isVerified;
    }

    // Getters
    public UserId getUserId() { return userId; }
    public String getId()     { return userId.getId(); }
    public String getName()   { return name; }
    public String getEmail()  { return email; }
}