package com.p2p.lending.infrastructure;

import com.p2p.lending.domain.model.Lender;
import com.p2p.lending.domain.repository.LenderRepository;
import java.util.HashMap;
import java.util.Map;

public class InMemoryLenderRepository implements LenderRepository {
    private final Map<String, Lender> store = new HashMap<>();

    @Override
    public void save(Lender lender) {
        store.put(lender.getId(), lender);
    }

    @Override
    public Lender findById(String lenderId) {
        return store.get(lenderId);
    }
}