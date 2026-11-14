package org.delcom.ifs25030p5pbo2026springbootcashflow.domain.entity;

import java.util.List;

/**
 * Ringkasan keuangan: jumlah transaksi, total pemasukan, total pengeluaran, dan saldo.
 */
public record Balance(int count, long totalIncome, long totalExpense) {

    /** Menghitung ringkasan dari daftar transaksi. */
    public static Balance of(List<Transaction> transactions) {
        long income = 0;
        long expense = 0;
        for (Transaction transaction : transactions) {
            if (transaction.isIncome()) {
                income += transaction.getAmount();
            } else {
                expense += transaction.getAmount();
            }
        }
        return new Balance(transactions.size(), income, expense);
    }

    /** Saldo = pemasukan - pengeluaran. */
    public long balance() {
        return totalIncome - totalExpense;
    }
}
