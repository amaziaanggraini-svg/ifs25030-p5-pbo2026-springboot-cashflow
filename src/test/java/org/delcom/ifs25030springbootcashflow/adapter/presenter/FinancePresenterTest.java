package org.delcom.ifs25030p5pbo2026springbootcashflow.adapter.presenter;

import org.delcom.ifs25030p5pbo2026springbootcashflow.domain.entity.Balance;
import org.delcom.ifs25030p5pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25030p5pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25030p5pbo2026springbootcashflow.domain.entity.TransactionType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FinancePresenterTest {
    private final FinancePresenter presenter = new FinancePresenter();
    private final Transaction income = new Transaction(
            1, 1, "Gaji", 1_000_000, TransactionType.INCOME, null, LocalDateTime.of(2026, 3, 4, 5, 6));
    private final Transaction expense = new Transaction(2, 1, "Makan", 25_000, TransactionType.EXPENSE);

    @Test
    void formatsMoney() {
        assertEquals("Rp 1,000,000.00", presenter.formatMoney(1_000_000));
        assertEquals("Rp 0.00", presenter.formatMoney(0));
        assertEquals("-Rp 1,500.00", presenter.formatMoney(-1_500));
    }

    @Test
    void formatsTransactionLine() {
        assertEquals("[1] Gaji | Rp 1,000,000.00 | [Pemasukan]", presenter.format(income));
        assertEquals("[Pengeluaran]", presenter.formatType(expense));
        assertEquals("04/03/2026 05:06", presenter.formatCreatedAt(income));
    }

    @Test
    void formatsBalance() {
        assertEquals("Saldo: Rp 4,975,000.00", presenter.formatBalance(new Balance(2, 5_000_000, 25_000)));
    }

    @Test
    void sortLabelsCoverAllOptions() {
        for (SortOption option : SortOption.values()) {
            assertFalse(presenter.sortLabel(option).isBlank());
        }
        assertEquals("Jumlah terkecil", presenter.sortLabel(SortOption.AMOUNT_ASC));
        assertEquals("Jumlah terbesar", presenter.sortLabel(SortOption.AMOUNT_DESC));
        assertEquals("Pemasukan dulu", presenter.sortLabel(SortOption.INCOME_FIRST));
        assertEquals("Pengeluaran dulu", presenter.sortLabel(SortOption.EXPENSE_FIRST));
    }

    @Test
    void listsAndMessages() {
        assertEquals("Daftar Transaksi:\n- Data transaksi belum tersedia!", presenter.showTransactions(List.of()));
        assertTrue(presenter.showTransactions(List.of(income, expense)).contains("[2] Makan"));
        assertTrue(presenter.showSearchResults(List.of(), "x").contains("tidak ditemukan"));
        assertTrue(presenter.showSearchResults(List.of(income), "gaji").contains("Gaji"));
        assertTrue(presenter.showSortedTransactions(List.of(income)).startsWith("Daftar Transaksi (Terurut):"));
        assertTrue(presenter.showAddSuccess(income).startsWith("Berhasil menambah pemasukan"));
        assertTrue(presenter.showAddSuccess(expense).contains("pengeluaran"));
        assertEquals("Berhasil menghapus transaksi.", presenter.showRemoveSuccess());
        assertTrue(presenter.showRemoveFailed(5).contains("5"));
        assertEquals("Berhasil mengubah transaksi.", presenter.showUpdateSuccess());
        assertTrue(presenter.showUpdateFailed(6).contains("6"));
        assertEquals("[!] ID tidak valid!", presenter.showInvalidId());
        assertEquals("[!] oops", presenter.showValidationError("oops"));
    }
}
