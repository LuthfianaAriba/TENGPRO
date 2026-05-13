package com.p2p.lending.domain.model;

import com.p2p.lending.domain.event.LoanAppliedEvent;
import com.p2p.lending.domain.observer.LoanFundingNotifier;
import com.p2p.lending.domain.state.AppliedState;
import com.p2p.lending.domain.state.FundingState;
import com.p2p.lending.domain.state.LoanState;
import com.p2p.lending.domain.valueobject.LoanStatus;
import com.p2p.lending.domain.valueobject.Money;
import com.p2p.lending.strategy.InterestStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * Loan adalah Aggregate Root.
 * 2 constructor:
 * - (id, targetAmount)                    -> untuk LoanStateTest & LoanApplicationTest
 * - (id, borrower, targetAmount, ...)     -> untuk InvestmentTest
 */
public class Loan {

    private String id;
    private Borrower borrower;
    private double targetAmount;
    private double fundedAmount;
    private double collectedAmount;
    private String purpose;
    private int tenorMonths;
    private double interestRate;
    private LoanStatus status;
    private LoanState state;
    private List<Investment> investments;
    private List<Object> domainEvents;

    // Strategy Pattern - untuk perhitungan bunga
    private InterestStrategy interestStrategy;

    private static final FundingState fundingStateHandler = new FundingState();

    // =============================================
    // CONSTRUCTOR 1 - LoanStateTest & LoanApplicationTest
    // new Loan("L001", 1000000)
    // =============================================
    public Loan(String id, double targetAmount) {
        this.id = id;
        this.targetAmount = targetAmount;
        this.collectedAmount = 0;
        this.fundedAmount = 0;
        this.status = LoanStatus.APPLIED;
        this.state = new AppliedState();
        this.investments = new ArrayList<>();
        this.domainEvents = new ArrayList<>();
        this.domainEvents.add(new LoanAppliedEvent(this.id, new Money((long) targetAmount)));
    }

    // =============================================
    // CONSTRUCTOR 2 - InvestmentTest
    // new Loan("LOAN-001", borrower, 1000000, "Modal Usaha", 12, 0.12)
    // =============================================
    public Loan(String id, Borrower borrower, double targetAmount,
                String purpose, int tenorMonths, double interestRate) {

        if (id == null || id.isBlank()) throw new IllegalArgumentException("Loan ID required");
        if (borrower == null) throw new IllegalArgumentException("Borrower required");
        if (targetAmount <= 0) throw new IllegalArgumentException("Target amount must be greater than 0");
        if (!borrower.isVerified()) throw new IllegalStateException("Borrower must be verified");

        this.id = id;
        this.borrower = borrower;
        this.targetAmount = targetAmount;
        this.fundedAmount = 0;
        this.collectedAmount = 0;
        this.purpose = purpose;
        this.tenorMonths = tenorMonths;
        this.interestRate = interestRate;
        this.status = LoanStatus.APPLIED;
        this.state = new AppliedState();
        this.investments = new ArrayList<>();
        this.domainEvents = new ArrayList<>();
    }

    // =============================================
    // STATE TRANSITIONS via LoanState
    // =============================================

    public void startFunding() {
        state.startFunding(this);
    }

    public void checkFundingCompletion() {
        if (collectedAmount >= targetAmount || fundedAmount >= targetAmount) {
            state.completeFunding(this);
        }
    }

    public void disburse() {
        state.disburse(this);
    }

    public void approve() {
        if (status != LoanStatus.APPLIED) {
            throw new IllegalStateException("Only APPLIED loans can be approved. Current: " + status);
        }
        this.status = LoanStatus.APPROVED;
    }

    public void reject() {
        if (status != LoanStatus.APPLIED) {
            throw new IllegalStateException("Only APPLIED loans can be rejected. Current: " + status);
        }
        this.status = LoanStatus.REJECTED;
    }

    // =============================================
    // FUNDING - didelegasikan ke FundingState
    // =============================================

    public void receiveFunding(double amount, Lender lender) {
        if (status != LoanStatus.FUNDING) {
            throw new IllegalStateException("Loan is not in FUNDING state. Current: " + status);
        }
        fundingStateHandler.receiveFunding(this, amount, lender);
    }

    // =============================================
    // METHODS UNTUK FundingState & LoanStateTest
    // =============================================

    public void addFundedAmount(double amount) {
        this.fundedAmount += amount;
        this.collectedAmount += amount;
    }

    public void addFunding(double amount) {
        this.collectedAmount += amount;
        this.fundedAmount += amount;
    }

    public void addInvestment(Investment investment) {
        this.investments.add(investment);
    }

    public int getInvestmentCount() {
        return investments.size();
    }

    public void setStatus(LoanStatus status) {
        this.status = status;
    }

    public void setState(LoanState state) {
        this.state = state;
    }

    // =============================================
    // STRATEGY PATTERN - Interest Calculation
    // =============================================

    public void setInterestStrategy(InterestStrategy strategy) {
        this.interestStrategy = strategy;
    }

    public double calculateMonthlyInstallment() {
        if (interestStrategy == null) return 0;
        return interestStrategy.calculateInterest(targetAmount, tenorMonths);
    }

    // =============================================
    // BUSINESS QUERIES
    // =============================================

    public double getRemainingFundingNeeded() {
        return targetAmount - fundedAmount;
    }

    public boolean isFullyFunded() {
        return fundedAmount >= targetAmount;
    }

    // =============================================
    // GETTERS
    // =============================================

    public String getId() { return id; }
    public String getLoanId() { return id; }
    public Borrower getBorrower() { return borrower; }
    public double getTargetAmount() { return targetAmount; }

    // return Money - untuk LoanApplicationTest
    public Money getFundedAmount() { return new Money((long) fundedAmount); }

    // return double - untuk InvestmentTest
    public double getFundedAmountAsDouble() { return fundedAmount; }

    public double getCollectedAmount() { return collectedAmount; }
    public String getPurpose() { return purpose; }
    public int getTenorMonths() { return tenorMonths; }
    public double getInterestRate() { return interestRate; }
    public LoanStatus getStatus() { return status; }
    public List<Investment> getInvestments() { return investments; }
    public List<Object> getDomainEvents() { return domainEvents; }
}
