package com.p2p.lending.infrastructure;

import com.p2p.lending.domain.model.Borrower;
import com.p2p.lending.domain.repository.BorrowerRepository;
import java.util.HashMap;
import java.util.Map;

public class InMemoryBorrowerRepository implements BorrowerRepository {
    private final Map<String, Borrower> store = new HashMap<>();

    @Override
    public void save(Borrower borrower) {
        store.put(borrower.getId(), borrower);
    }

    @Override
    public Borrower findById(String borrowerId) {
        return store.get(borrowerId);
    }
}