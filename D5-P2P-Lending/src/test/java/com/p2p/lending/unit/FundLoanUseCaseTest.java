package com.p2p.lending.unit;

import com.p2p.lending.application.dto.LoanResponse;
import com.p2p.lending.application.usecase.FundLoanUseCase;
import com.p2p.lending.domain.model.Borrower;
import com.p2p.lending.domain.model.Lender;
import com.p2p.lending.domain.model.Loan;
import com.p2p.lending.domain.repository.LenderRepository;
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

public class FundLoanUseCaseTest {

    private FundLoanUseCase fundLoanUseCase;
    private LoanRepository loanRepository;
    private LenderRepository lenderRepository;
    private Loan loan;
    private Lender lender;

    @BeforeEach
    public void setUp() {
        loanRepository = mock(LoanRepository.class);
        lenderRepository = mock(LenderRepository.class);
        fundLoanUseCase = new FundLoanUseCase(loanRepository, lenderRepository);

        Borrower borrower = new Borrower(new UserId("B001"));
        borrower.setVerified(true);
        loan = borrower.applyForLoan(new Money(1_000_000));
        loan.startFunding();

        lender = new Lender("L001", "Lina", "lina@email.com", 2_000_000);
    }

    @AfterEach
    public void tearDown() {
        fundLoanUseCase = null;
        loanRepository = null;
        lenderRepository = null;
        loan = null;
        lender = null;
    }

    @Test
    public void testFundLoan_success() {
        // Given
        when(loanRepository.findById(loan.getLoanId())).thenReturn(Optional.of(loan));
        when(lenderRepository.findById("L001")).thenReturn(lender);
        doNothing().when(loanRepository).save(any());

        // When
        LoanResponse response = fundLoanUseCase.execute(loan.getLoanId(), "L001", 500_000);

        // Then
        assertNotNull(response);
        assertEquals(500_000, response.getFundedAmount());
        verify(loanRepository, times(1)).save(any());
    }

    @Test
    public void testFundLoan_throwsException_whenLoanNotFound() {
        // Given
        when(loanRepository.findById(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            fundLoanUseCase.execute("LOAN-999", "L001", 500_000);
        });
    }

    @Test
    public void testFundLoan_throwsException_whenLenderNotFound() {
        // Given
        when(loanRepository.findById(loan.getLoanId())).thenReturn(Optional.of(loan));
        when(lenderRepository.findById("L999")).thenReturn(null);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            fundLoanUseCase.execute(loan.getLoanId(), "L999", 500_000);
        });
    }
}