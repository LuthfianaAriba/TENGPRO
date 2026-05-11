/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.p2p.lending.domain.state;

import com.p2p.lending.domain.model.Loan;

/**
 *
 * @author febianaafra
 */

public interface LoanState {
    void startFunding(Loan loan);
    void completeFunding(Loan loan);
    void disburse(Loan loan);
}