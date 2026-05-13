package com.p2p.lending.unit;

import com.p2p.lending.application.dto.LoanRequest;
import com.p2p.lending.application.dto.LoanResponse;
import com.p2p.lending.application.usecase.ApplyLoanUseCase;
import com.p2p.lending.domain.model.Borrower;
import com.p2p.lending.domain.repository.BorrowerRepository;
import com.p2p.lending.domain.repository.LoanRepository;
import com.p2p.lending.domain.valueobject.UserId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

public class ApplyLoanUseCaseTest {

    private ApplyLoanUseCase applyLoanUseCase;
    private BorrowerRepository borrowerRepository;
    private LoanRepository loanRepository;
    private Borrower verifiedBorrower;

    @BeforeEach
    public void setUp() {
        // Mock repositories
        borrowerRepository = mock(BorrowerRepository.class);
        loanRepository = mock(LoanRepository.class);

        // Inject ke UseCase
        applyLoanUseCase = new ApplyLoanUseCase(borrowerRepository, loanRepository);

        // Setup verified borrower
        verifiedBorrower = new Borrower(new UserId("B001"));
        verifiedBorrower.setVerified(true);
    }

    @AfterEach
    public void tearDown() {
        applyLoanUseCase = null;
        borrowerRepository = null;
        loanRepository = null;
        verifiedBorrower = null;
    }

    @Test
    public void testApplyLoan_success() {
        // Given
        when(borrowerRepository.findById("B001")).thenReturn(verifiedBorrower);
        doNothing().when(loanRepository).save(any());

        LoanRequest request = new LoanRequest("B001", 1_000_000, "Modal Usaha", 12, 0.12);

        // When
        LoanResponse response = applyLoanUseCase.execute(request);

        // Then
        assertNotNull(response, "Response tidak boleh null");
        assertEquals("B001", response.getBorrowerId());
        assertEquals(1_000_000, response.getTargetAmount());
        verify(loanRepository, times(1)).save(any());
    }

    @Test
    public void testApplyLoan_throwsException_whenBorrowerNotFound() {
        // Given
        when(borrowerRepository.findById("B999")).thenReturn(null);

        LoanRequest request = new LoanRequest("B999", 1_000_000, "Modal Usaha", 12, 0.12);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            applyLoanUseCase.execute(request);
        }, "Harus throw exception jika borrower tidak ditemukan");
    }
}
