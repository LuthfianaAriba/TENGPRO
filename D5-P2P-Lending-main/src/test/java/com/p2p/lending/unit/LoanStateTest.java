/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.p2p.lending.unit;

/**
 *
 * @author febianaafra
 */

import com.p2p.lending.domain.model.Loan;
import com.p2p.lending.domain.valueobject.LoanStatus;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LoanStateTest {

    @Test
    public void testInitialStateIsApplied() {
        Loan loan = new Loan("L001", 1000000);
        assertEquals(LoanStatus.APPLIED, loan.getStatus());
    }

    @Test
    public void testAppliedToFunding() {
        Loan loan = new Loan("L001", 1000000);
        loan.startFunding();
        assertEquals(LoanStatus.FUNDING, loan.getStatus());
    }

    @Test
    public void testFundingToFullyFunded() {
        Loan loan = new Loan("L001", 1000000);
        loan.startFunding();
        loan.addFunding(1000000);
        loan.checkFundingCompletion();
        assertEquals(LoanStatus.FULLY_FUNDED, loan.getStatus());
    }
}