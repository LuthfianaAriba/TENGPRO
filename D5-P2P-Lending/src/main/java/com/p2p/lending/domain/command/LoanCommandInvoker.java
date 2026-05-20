package com.p2p.lending.domain.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Command Pattern - LoanCommandInvoker (Invoker)
 *
 * Bertanggung jawab menjalankan command dan menyimpan
 * history audit trail seluruh operasi yang sudah dieksekusi.
 *
 * Peran dalam pattern:
 *   - Invoker memegang referensi command dan memanggilnya
 *   - Client (test / use case) hanya membuat command & menyerahkan ke invoker
 *   - History berguna untuk audit log di sistem P2P Lending
 */
public class LoanCommandInvoker {

    private final List<String> commandHistory = new ArrayList<>();

    /**
     * Eksekusi sebuah command dan catat ke history.
     * Jika command gagal (exception), tetap dicatat dengan prefix ERROR.
     *
     * @param command command yang akan dieksekusi
     * @throws RuntimeException jika command.execute() melempar exception
     */
    public void invoke(LoanCommand command) {
        try {
            command.execute();
            commandHistory.add("[SUCCESS] " + command.getCommandName());
        } catch (Exception e) {
            commandHistory.add("[ERROR] " + command.getCommandName() + " - " + e.getMessage());
            throw e;
        }
    }

    /**
     * Mengembalikan history eksekusi command (read-only).
     */
    public List<String> getCommandHistory() {
        return Collections.unmodifiableList(commandHistory);
    }

    /**
     * Mengembalikan jumlah command yang sudah dieksekusi (sukses maupun gagal).
     */
    public int getHistorySize() {
        return commandHistory.size();
    }

    /**
     * Reset history — berguna untuk membersihkan state antar test.
     */
    public void clearHistory() {
        commandHistory.clear();
    }
}
