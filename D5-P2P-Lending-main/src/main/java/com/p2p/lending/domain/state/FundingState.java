package com.p2p.lending.domain.state;

import com.p2p.lending.domain.model.Investment;
import com.p2p.lending.domain.model.Lender;
import com.p2p.lending.domain.model.Loan;
import com.p2p.lending.domain.observer.LoanFundingNotifier;
import com.p2p.lending.domain.valueobject.LoanStatus;

public class FundingState implements LoanState {

    // =============================================
    // IMPLEMENTS LoanState
    // =============================================

    @Override
    public void startFunding(Loan loan) {
        throw new IllegalStateException("Loan is already in FUNDING state");
    }

    @Override
    public void completeFunding(Loan loan) {
        loan.setStatus(LoanStatus.FULLY_FUNDED);
        loan.setState(new FullyFundedState());
    }

    @Override
    public void disburse(Loan loan) {
        throw new IllegalStateException("Cannot disburse, loan is not fully funded yet");
    }

    // =============================================
    // FUNDING LOGIC
    // =============================================

    public void receiveFunding(Loan loan, double amount, Lender lender) {
        // 1. Validasi amount
        if (amount <= 0) {
            throw new IllegalArgumentException("Invalid funding amount");
        }
        // 2. Validasi saldo lender
        if (!lender.canInvest(amount)) {
            throw new IllegalStateException("Insufficient balance");
        }
        // 3. Validasi overfund
        if (loan.getFundedAmountAsDouble() + amount > loan.getTargetAmount()) {
            throw new IllegalArgumentException("Exceeds loan target amount");
        }
        // 4. Tambah funding
        loan.addFundedAmount(amount);
        // 5. Simpan investment
        loan.addInvestment(new Investment(
                "INV-" + (loan.getInvestmentCount()),
                lender,
                loan.getId(),
                amount
        ));
        // 6. Kurangi saldo lender
        lender.invest(amount);
        // 7. Update status kalau penuh + trigger Observer
        if (loan.getFundedAmountAsDouble() >= loan.getTargetAmount()) {
            completeFunding(loan);
            // Observer Pattern — notify semua observer via Singleton Notifier
            LoanFundingNotifier.getInstance().notifyFullyFunded(loan);
        }
    }
}
