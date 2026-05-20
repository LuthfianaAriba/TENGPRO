package com.p2p.lending.domain.decorator;

/**
 * Decorator Pattern - Concrete Decorator
 * Menambahkan biaya denda keterlambatan ke loan.
 */
public class PenaltyDecorator extends LoanDecorator {

    private final double penalty;

    public PenaltyDecorator(LoanComponent loan, double penalty) {
        super(loan);
        if (penalty < 0) throw new IllegalArgumentException("Penalty tidak boleh negatif");
        this.penalty = penalty;
    }

    @Override
    public double getCost() {
        return loan.getCost() + penalty;
    }

    @Override
    public String getDescription() {
        return loan.getDescription() + " + Penalty: " + penalty;
    }
}