package com.p2p.lending.domain.repository;

import com.p2p.lending.domain.model.Borrower;

public interface BorrowerRepository {

    void save(Borrower borrower);

    Borrower findById(String borrowerId);
}