package com.p2p.lending.strategy;

public class EffectiveInterestStrategy implements InterestStrategy {
    private final double annualRate;

    public EffectiveInterestStrategy(double annualRate) {
        this.annualRate = annualRate;
    }

    @Override
    public double calculateInterest(double principal, int tenorMonths) {
        double monthlyRate = annualRate / 12;
        double totalPayment = principal * monthlyRate
                * Math.pow(1 + monthlyRate, tenorMonths)
                / (Math.pow(1 + monthlyRate, tenorMonths) - 1)
                * tenorMonths;
        return totalPayment - principal;
    }
}