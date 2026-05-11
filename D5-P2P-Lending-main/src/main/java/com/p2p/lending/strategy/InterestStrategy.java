package com.p2p.lending.strategy;

public interface InterestStrategy {
    double calculateInterest(double principal, int tenorMonths);
}