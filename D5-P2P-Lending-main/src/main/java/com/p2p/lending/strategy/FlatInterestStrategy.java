package com.p2p.lending.strategy;

public class FlatInterestStrategy implements InterestStrategy {
    private final double annualRate;

    public FlatInterestStrategy(double annualRate) {
        this.annualRate = annualRate;
    }

    @Override
    public double calculateInterest(double principal, int tenorMonths) {
        return principal * annualRate * (tenorMonths / 12.0);
    }
}