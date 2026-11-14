package org.delcom.ifs25030springbootcashflow.usecase;

import org.delcom.ifs25030springbootcashflow.domain.entity.Balance;
import org.delcom.ifs25030springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25030springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25030springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25030springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25030springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25030springbootcashflow.domain.repository.ITransactionRepository;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * Use case logika bisnis pencatatan keuangan.
 * Tidak melakukan I/O UI: hanya memproses data dan melempar checked exception domain
 * bila input tidak valid atau data tidak ditemukan. Semua operasi di-scope per pengguna.
 */
public class FinanceUseCase {
    private final ITransactionRepository transactionRepository;

    public FinanceUseCase(ITransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /** Mengambil semua transaksi milik satu pengguna. */
    public List<Transaction> getTransactionsByUser(int userId) {
        return transactionRepository.findByUserId(userId);
    }

    /** Menambah pemasukan. */
    public Transaction addIncome(int userId, String description, String amountText) throws ValidationException {
        return addTransaction(userId, TransactionType.INCOME, description, amountText);
    }

    /** Menambah pengeluaran. */
    public Transaction addExpense(int userId, String description, String amountText) throws ValidationException {
        return addTransaction(userId, TransactionType.EXPENSE, description, amountText);
    }

    /** Menambah transaksi setelah validasi keterangan dan jumlah. */
    public Transaction addTransaction(int userId, TransactionType type, String description, String amountText)
            throws ValidationException {
        String safeDescription = requireDescription(description);
        long amount = parseAmount(amountText);
        return transactionRepository.save(userId, safeDescription, amount, type);
    }

    /** Menghapus transaksi milik pengguna tertentu. */
    public void removeTransactionForUser(int userId, int id) throws EntityNotFoundException, ValidationException {
        Transaction transaction = requireOwned(userId, id);
        if (!transactionRepository.deleteById(transaction.getId())) {
            throw new EntityNotFoundException("Transaksi", id);
        }
    }

    /**
     * Mengubah transaksi milik pengguna. Parameter {@code null} berarti field tidak diubah
     * (kecuali {@code clearProof=true} untuk menghapus bukti).
     */
    public void updateTransactionForUser(
            int userId,
            int id,
            String description,
            String amountText,
            TransactionType type,
            String proofPath,
            boolean clearProof
    ) throws EntityNotFoundException, ValidationException {
        Transaction transaction = requireOwned(userId, id);
        if (description != null) {
            transaction.changeDescription(requireDescription(description));
        }
        if (amountText != null) {
            transaction.changeAmount(parseAmount(amountText));
        }
        if (type != null) {
            transaction.changeType(type);
        }
        if (clearProof) {
            transaction.changeProofPath(null);
        } else if (proofPath != null) {
            transaction.changeProofPath(proofPath);
        }
        transactionRepository.update(transaction);
    }

    /** Mencari transaksi pengguna yang keterangannya mengandung kata kunci (case-insensitive). */
    public List<Transaction> searchTransactions(int userId, String keyword) {
        return filter(transactionRepository.findByUserId(userId), keyword).toList();
    }

    /** Mengurutkan transaksi pengguna sesuai {@link SortOption}. */
    public List<Transaction> sortTransactions(int userId, SortOption option) {
        return transactionRepository.findByUserId(userId).stream()
                .sorted(option.comparator())
                .toList();
    }

    /** Pencarian + pengurutan sekaligus; keyword kosong = semua, option null = urutan asli. */
    public List<Transaction> findTransactions(int userId, String keyword, SortOption option) {
        Stream<Transaction> stream = filter(transactionRepository.findByUserId(userId), keyword);
        if (option != null) {
            stream = stream.sorted(option.comparator());
        }
        return stream.toList();
    }

    /** Menghitung ringkasan total, pemasukan, pengeluaran, dan saldo pengguna. */
    public Balance getBalance(int userId) {
        return Balance.of(transactionRepository.findByUserId(userId));
    }

    private static Stream<Transaction> filter(List<Transaction> source, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return source.stream();
        }
        String lower = keyword.trim().toLowerCase(Locale.ROOT);
        return source.stream()
                .filter(t -> t.getDescription().toLowerCase(Locale.ROOT).contains(lower));
    }

    private Transaction requireOwned(int userId, int id) throws EntityNotFoundException, ValidationException {
        if (id <= 0) {
            throw new ValidationException("ID tidak valid!");
        }
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transaksi", id));
        if (transaction.getUserId() != userId) {
            throw new ValidationException("Transaksi tidak milik pengguna ini");
        }
        return transaction;
    }

    private static String requireDescription(String description) throws ValidationException {
        if (description == null || description.trim().isEmpty()) {
            throw new ValidationException("Keterangan tidak boleh kosong");
        }
        return description.trim();
    }

    /** Mengubah teks jumlah menjadi angka positif; menolak kosong, non-numerik, dan non-positif. */
    static long parseAmount(String amountText) throws ValidationException {
        if (amountText == null || amountText.trim().isEmpty()) {
            throw new ValidationException("Jumlah tidak boleh kosong");
        }
        long amount;
        try {
            amount = Long.parseLong(amountText.trim());
        } catch (NumberFormatException ex) {
            throw new ValidationException("Jumlah harus berupa angka bulat");
        }
        if (amount <= 0) {
            throw new ValidationException("Jumlah harus lebih besar dari 0");
        }
        return amount;
    }
}
