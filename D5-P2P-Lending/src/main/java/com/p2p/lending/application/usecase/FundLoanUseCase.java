// FundLoanUseCase.java
package com.p2p.lending.application.usecase;

import com.p2p.lending.application.dto.LoanResponse;
import com.p2p.lending.domain.model.Lender;
import com.p2p.lending.domain.model.Loan;
import com.p2p.lending.domain.repository.LenderRepository;
import com.p2p.lending.domain.repository.LoanRepository;

public class FundLoanUseCase {

    private final LoanRepository loanRepository;
    private final LenderRepository lenderRepository;

    public FundLoanUseCase(LoanRepository loanRepository,
                           LenderRepository lenderRepository) {
        this.loanRepository = loanRepository;
        this.lenderRepository = lenderRepository;
    }

    public LoanResponse execute(String loanId, String lenderId, double amount) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found"));

        Lender lender = lenderRepository.findById(lenderId);
        if (lender == null) {
            throw new IllegalArgumentException("Lender not found");
        }

        loan.receiveFunding(amount, lender);
        loanRepository.save(loan);

        return new LoanResponse(
            loan.getLoanId(),
            loan.getBorrower() != null ? loan.getBorrower().getId() : null,
            loan.getTargetAmount(),
            loan.getFundedAmountAsDouble(),
            loan.getStatus()
        );
    }
}