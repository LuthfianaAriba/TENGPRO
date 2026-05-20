package com.p2p.lending.domain.repository;
import com.p2p.lending.domain.model.Loan;
import java.util.Optional;

public interface LoanRepository {
    void save(Loan loan);
    Optional<Loan> findById(String id);
}