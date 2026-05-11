// ApplyLoanUseCase.java
package com.p2p.lending.application.usecase;

import com.p2p.lending.application.dto.LoanRequest;
import com.p2p.lending.application.dto.LoanResponse;
import com.p2p.lending.domain.model.Borrower;
import com.p2p.lending.domain.model.Loan;
import com.p2p.lending.domain.repository.BorrowerRepository;
import com.p2p.lending.domain.repository.LoanRepository;
import com.p2p.lending.domain.valueobject.Money;

public class ApplyLoanUseCase {

    private final BorrowerRepository borrowerRepository;
    private final LoanRepository loanRepository;

    public ApplyLoanUseCase(BorrowerRepository borrowerRepository,
                            LoanRepository loanRepository) {
        this.borrowerRepository = borrowerRepository;
        this.loanRepository = loanRepository;
    }

    public LoanResponse execute(LoanRequest request) {
        Borrower borrower = borrowerRepository.findById(request.getBorrowerId());
        if (borrower == null) {
            throw new IllegalArgumentException("Borrower not found");
        }

        Loan loan = borrower.applyForLoan(new Money((long) request.getAmount()));
        loanRepository.save(loan);

        return new LoanResponse(
            loan.getLoanId(),
            borrower.getId(),
            loan.getTargetAmount(),
            loan.getFundedAmountAsDouble(),
            loan.getStatus()
        );
    }
}