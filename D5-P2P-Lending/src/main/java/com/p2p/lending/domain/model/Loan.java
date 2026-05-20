package com.p2p.lending.domain.model;

import com.p2p.lending.domain.event.LoanAppliedEvent;
import com.p2p.lending.domain.state.AppliedState;
import com.p2p.lending.domain.state.LoanState;
import com.p2p.lending.domain.valueobject.LoanStatus;
import com.p2p.lending.domain.valueobject.Money;
import com.p2p.lending.domain.interest.strategy.InterestStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * Loan - Aggregate Root
 *
 * Refactoring yang dilakukan (Martin Fowler):
 * 1. Remove Duplicate Field  — collectedAmount dihapus, pakai fundedAmount saja
 * 2. Remove Duplicate Method — addFunding() dihapus, pakai addFundedAmount() saja
 * 3. Remove Duplicate Getter — getLoanId() dihapus, pakai getId() saja
 * 4. Extract Method          — validateStatusIs() untuk validasi state transition
 * 5. Remove Duplicate Method — receiveFunding() duplikat dihapus, pakai State Pattern
 */
public class Loan {

    private String id;
    private Borrower borrower;
    private double targetAmount;
    private double fundedAmount;
    private String purpose;
    private int tenorMonths;
    private double interestRate;
    private LoanStatus status;
    private LoanState state;
    private List<Investment> investments;
    private List<Object> domainEvents;
    private InterestStrategy interestStrategy;

    // =============================================
    // CONSTRUCTOR 1 - untuk LoanStateTest & LoanApplicationTest
    // new Loan("L001", 1000000)
    // =============================================
    public Loan(String id, double targetAmount) {
        this.id = id;
        this.targetAmount = targetAmount;
        this.fundedAmount = 0;
        this.status = LoanStatus.APPLIED;
        this.state = new AppliedState();
        this.investments = new ArrayList<>();
        this.domainEvents = new ArrayList<>();
        this.domainEvents.add(new LoanAppliedEvent(this.id, new Money((long) targetAmount)));
    }

    // =============================================
    // CONSTRUCTOR 2 - untuk InvestmentTest
    // new Loan("LOAN-001", borrower, 1000000, "Modal Usaha", 12, 0.12)
    // =============================================
    public Loan(String id, Borrower borrower, double targetAmount,
                String purpose, int tenorMonths, double interestRate) {

        validateId(id);
        validateBorrower(borrower);
        validateAmount(targetAmount);

        this.id = id;
        this.borrower = borrower;
        this.targetAmount = targetAmount;
        this.fundedAmount = 0;
        this.purpose = purpose;
        this.tenorMonths = tenorMonths;
        this.interestRate = interestRate;
        this.status = LoanStatus.APPLIED;
        this.state = new AppliedState();
        this.investments = new ArrayList<>();
        this.domainEvents = new ArrayList<>();
    }

    // =============================================
    // PRIVATE VALIDATION METHODS (Extract Method)
    // =============================================

    private void validateId(String id) {
        if (id == null || id.isBlank())
            throw new IllegalArgumentException("Loan ID required");
    }

    private void validateBorrower(Borrower borrower) {
        if (borrower == null)
            throw new IllegalArgumentException("Borrower required");
        if (!borrower.isVerified())
            throw new IllegalStateException("Borrower must be verified");
    }

    private void validateAmount(double amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Target amount must be greater than 0");
    }

    /**
     * Extract Method — validasi status sebelum transisi
     * Dipakai oleh approve() dan reject()
     */
    private void validateStatusIs(LoanStatus expected, String action) {
        if (status != expected) {
            throw new IllegalStateException(
                "Only " + expected + " loans can be " + action + ". Current: " + status);
        }
    }

    // =============================================
    // STATE TRANSITIONS via LoanState
    // =============================================

    public void startFunding() {
        state.startFunding(this);
    }

    public void checkFundingCompletion() {
        if (isFullyFunded()) {
            state.completeFunding(this);
        }
    }

    public void disburse() {
        state.disburse(this);
    }

    public void approve() {
        validateStatusIs(LoanStatus.APPLIED, "approved");
        this.status = LoanStatus.APPROVED;
    }

    public void reject() {
        validateStatusIs(LoanStatus.APPLIED, "rejected");
        this.status = LoanStatus.REJECTED;
    }

    // =============================================
    // FUNDING - didelegasikan ke State Pattern
    // =============================================

    public void receiveFunding(double amount, Lender lender) {
        state.receiveFunding(this, amount, lender);
    }

    // =============================================
    // FUNDING HELPER METHODS
    // =============================================

    public void addFundedAmount(double amount) {
        this.fundedAmount += amount;
    }

    /**
     * @deprecated pakai addFundedAmount() — dihapus duplikasi
     */
    public void addFunding(double amount) {
        addFundedAmount(amount);
    }

    public void addInvestment(Investment investment) {
        this.investments.add(investment);
    }

    public int getInvestmentCount() {
        return investments.size();
    }

    // =============================================
    // STATE SETTERS (untuk dipakai LoanState)
    // =============================================

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

    /**
     * @deprecated pakai getId() — dihapus duplikasi
     */
    public String getLoanId() { return id; }

    public Borrower getBorrower() { return borrower; }
    public double getTargetAmount() { return targetAmount; }

    // return Money - untuk LoanApplicationTest
    public Money getFundedAmount() { return new Money((long) fundedAmount); }

    // return double - untuk InvestmentTest & LoanServiceTest
    public double getFundedAmountAsDouble() { return fundedAmount; }

    /**
     * @deprecated pakai getFundedAmountAsDouble() — collectedAmount dihapus duplikasi
     */
    public double getCollectedAmount() { return fundedAmount; }

    public String getPurpose() { return purpose; }
    public int getTenorMonths() { return tenorMonths; }
    public double getInterestRate() { return interestRate; }
    public LoanStatus getStatus() { return status; }
    public List<Investment> getInvestments() { return investments; }
    public List<Object> getDomainEvents() { return domainEvents; }
}