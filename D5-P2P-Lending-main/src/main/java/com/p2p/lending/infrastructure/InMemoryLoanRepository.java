package com.p2p.lending.infrastructure;
import com.p2p.lending.domain.model.Loan;
import com.p2p.lending.domain.repository.LoanRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryLoanRepository implements LoanRepository {
    private Map<String, Loan> storage = new HashMap<>();

    @Override
    public void save(Loan loan) {
        storage.put(loan.getId(), loan);
    }

    @Override
    public Optional<Loan> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }
}