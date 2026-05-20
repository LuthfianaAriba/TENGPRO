package com.p2p.lending.domain.repository;

import com.p2p.lending.domain.model.Lender;

public interface LenderRepository {

    void save(Lender lender);

    Lender findById(String lenderId);
}