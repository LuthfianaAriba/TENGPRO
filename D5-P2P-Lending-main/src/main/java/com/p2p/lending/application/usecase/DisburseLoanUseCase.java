// DisburseLoanUseCase.java
package com.p2p.lending.application.usecase;

import com.p2p.lending.application.dto.LoanResponse;
import com.p2p.lending.domain.model.Loan;
import com.p2p.lending.domain.repository.LoanRepository;

public class DisburseLoanUseCase {

    private final LoanRepository loanRepository;

    public DisburseLoanUseCase(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    public LoanResponse execute(String loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found"));

        loan.disburse();
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