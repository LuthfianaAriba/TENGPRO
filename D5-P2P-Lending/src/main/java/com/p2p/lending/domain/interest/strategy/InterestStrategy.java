package com.p2p.lending.domain.interest.strategy;

public interface InterestStrategy {
    double calculateInterest(double principal, int tenorMonths);
}