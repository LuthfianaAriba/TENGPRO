package com.p2p.lending.unit;

import com.p2p.lending.application.dto.LoanResponse;
import com.p2p.lending.application.usecase.DisburseLoanUseCase;
import com.p2p.lending.domain.model.Borrower;
import com.p2p.lending.domain.model.Lender;
import com.p2p.lending.domain.model.Loan;
import com.p2p.lending.domain.repository.LoanRepository;
import com.p2p.lending.domain.valueobject.Money;
import com.p2p.lending.domain.valueobject.UserId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

import java.util.Optional;

public class DisburseLoanUseCaseTest {

    private DisburseLoanUseCase disburseLoanUseCase;
    private LoanRepository loanRepository;
    private Loan fullyFundedLoan;

    @BeforeEach
    public void setUp() {
        loanRepository = mock(LoanRepository.class);
        disburseLoanUseCase = new DisburseLoanUseCase(loanRepository);

        // Setup loan yang sudah fully funded
        Borrower borrower = new Borrower(new UserId("B001"));
        borrower.setVerified(true);
        fullyFundedLoan = borrower.applyForLoan(new Money(1_000_000));
        fullyFundedLoan.startFunding();

        Lender lender = new Lender("L001", "Lina", "lina@email.com", 2_000_000);
        fullyFundedLoan.receiveFunding(1_000_000, lender);
    }

    @AfterEach
    public void tearDown() {
        disburseLoanUseCase = null;
        loanRepository = null;
        fullyFundedLoan = null;
    }

    @Test
    public void testDisburseLoan_success() {
        // Given
        when(loanRepository.findById(fullyFundedLoan.getLoanId()))
                .thenReturn(Optional.of(fullyFundedLoan));
        doNothing().when(loanRepository).save(any());

        // When
        LoanResponse response = disburseLoanUseCase.execute(fullyFundedLoan.getLoanId());

        // Then
        assertNotNull(response);
        verify(loanRepository, times(1)).save(any());
    }

    @Test
    public void testDisburseLoan_throwsException_whenLoanNotFound() {
        // Given
        when(loanRepository.findById(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            disburseLoanUseCase.execute("LOAN-999");
        });
    }
}
