package com.p2p.lending.domain.decorator;

/**
 * Decorator Pattern - Component Interface
 * Semua loan (simple maupun decorated) harus implements ini.
 */
public interface LoanComponent {
    double getCost();
    String getDescription();
}