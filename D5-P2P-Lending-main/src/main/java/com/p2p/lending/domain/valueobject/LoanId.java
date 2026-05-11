package com.p2p.lending.domain.valueobject;

import java.util.UUID;

public class LoanId {
    private final String id;

    public LoanId(String id) {
        if (id == null || id.isBlank())
            throw new IllegalArgumentException("LoanId required");
        this.id = id;
    }

    public static LoanId generate() {
        return new LoanId(UUID.randomUUID().toString());
    }

    public String getId() { return id; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LoanId)) return false;
        return id.equals(((LoanId) o).id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }

    @Override
    public String toString() { return id; }
}