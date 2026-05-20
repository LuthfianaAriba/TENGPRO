package com.p2p.lending.domain.model;

import com.p2p.lending.domain.valueobject.Money;
import com.p2p.lending.domain.valueobject.UserId;
import java.util.UUID;

public class Borrower extends User {

    /**
     * @param creditScore the creditScore to set
     */
    public void setCreditScore(int creditScore) {
        this.creditScore = creditScore;
    }

    private int creditScore;

    // Constructor 1 - LoanApplicationTest & LoanValidationStepDef
    // new Borrower(new UserId("B001"))
    public Borrower(UserId userId) {
        super(userId);
    }

    // Constructor 2 - InvestmentTest
    // new Borrower("BR001", "Budi", "budi@email.com", 700)
    public Borrower(String id, String name, String email, int creditScore) {
        super(id, name, email);
        if (creditScore < 0)
            throw new IllegalArgumentException("Credit score cannot be negative");
        this.creditScore = creditScore;
    }

    // =============================================
    // APPLY FOR LOAN
    // =============================================
    public Loan applyForLoan(Money amount) {
        if (!isVerified) {
            throw new IllegalStateException("Borrower not verified");
        }
        if (amount.getAmount() <= 0) {
            throw new IllegalArgumentException("Invalid loan amount");
        }
        String newLoanId = UUID.randomUUID().toString();
        return new Loan(newLoanId, (double) amount.getAmount());
    }

    // =============================================
    // GETTER
    // =============================================
    public int getCreditScore() { return creditScore; }
}