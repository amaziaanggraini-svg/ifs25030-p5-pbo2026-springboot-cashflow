package org.delcom.ifs25030springbootcashflow.adapter.presenter;

import org.delcom.ifs25030springbootcashflow.domain.entity.Balance;
import org.delcom.ifs25030springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25030springbootcashflow.domain.entity.Transaction;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Presenter yang memformat data dari use case menjadi teks siap tampil.
 * Format uang dan teks bukan tanggung jawab entity: bisa diubah tanpa menyentuh domain.
 */
public class FinancePresenter {

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Memformat uang: {@code Rp 1,000,000.00}; nilai negatif menjadi {@code -Rp 1,000.00}. */
    public String formatMoney(long amount) {
        String sign = amount < 0 ? "-" : "";
        return sign + String.format(Locale.US, "Rp %,d.00", Math.abs(amount));
    }

    /** Memformat label jenis transaksi: {@code [Pemasukan]} / {@code [Pengeluaran]}. */
    public String formatType(Transaction transaction) {
        return "[" + transaction.getType().label() + "]";
    }

    /** Memformat satu transaksi: {@code [id] keterangan | Rp x | [Jenis]}. */
    public String format(Transaction transaction) {
        return String.format(
                Locale.US,
                "[%d] %s | %s | %s",
                transaction.getId(),
                transaction.getDescription(),
                formatMoney(transaction.getAmount()),
                formatType(transaction)
        );
    }

    /** Memformat waktu pencatatan transaksi. */
    public String formatCreatedAt(Transaction transaction) {
        return transaction.getCreatedAt().format(DATE_TIME_FORMAT);
    }

    /** Memformat baris saldo: {@code Saldo: Rp 4,975,000.00}. */
    public String formatBalance(Balance balance) {
        return "Saldo: " + formatMoney(balance.balance());
    }

    /** Label tampilan opsi pengurutan. */
    public String sortLabel(SortOption option) {
        return switch (option) {
            case AMOUNT_ASC -> "Jumlah terkecil";
            case AMOUNT_DESC -> "Jumlah terbesar";
            case INCOME_FIRST -> "Pemasukan dulu";
            case EXPENSE_FIRST -> "Pengeluaran dulu";
        };
    }

    /** Memformat daftar transaksi menjadi teks multi-baris. */
    public String formatList(List<Transaction> transactions, String header, String emptyMessage) {
        if (transactions.isEmpty()) {
            return header + "\n" + emptyMessage;
        }
        String body = transactions.stream()
                .map(this::format)
                .collect(Collectors.joining("\n"));
        return header + "\n" + body;
    }

    public String showTransactions(List<Transaction> transactions) {
        return formatList(transactions, "Daftar Transaksi:", "- Data transaksi belum tersedia!");
    }

    public String showSearchResults(List<Transaction> transactions, String keyword) {
        return formatList(transactions, "Hasil Pencarian: \"" + keyword + "\"", "- Transaksi tidak ditemukan!");
    }

    public String showSortedTransactions(List<Transaction> transactions) {
        return formatList(transactions, "Daftar Transaksi (Terurut):", "- Data transaksi belum tersedia!");
    }

    public String showAddSuccess(Transaction transaction) {
        return "Berhasil menambah " + transaction.getType().label().toLowerCase(Locale.ROOT)
                + ": " + format(transaction);
    }

    public String showRemoveSuccess() {
        return "Berhasil menghapus transaksi.";
    }

    public String showRemoveFailed(int id) {
        return String.format("[!] Gagal menghapus transaksi dengan ID: %d.", id);
    }

    public String showUpdateSuccess() {
        return "Berhasil mengubah transaksi.";
    }

    public String showUpdateFailed(int id) {
        return String.format("[!] Gagal mengubah transaksi dengan ID: %d.", id);
    }

    public String showInvalidId() {
        return "[!] ID tidak valid!";
    }

    public String showValidationError(String message) {
        return "[!] " + message;
    }
}
