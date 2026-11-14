package org.delcom.ifs25030p5pbo2026springbootcashflow.domain.entity;

/**
 * Jenis transaksi keuangan.
 * Pemasukan menambah saldo, pengeluaran mengurangi saldo.
 */
public enum TransactionType {
    INCOME("Pemasukan", 1),
    EXPENSE("Pengeluaran", -1);

    private final String label;
    private final int sign;

    TransactionType(String label, int sign) {
        this.label = label;
        this.sign = sign;
    }

    /** Nama tampilan dalam bahasa Indonesia. */
    public String label() {
        return label;
    }

    /** Pengali saldo: +1 untuk pemasukan, -1 untuk pengeluaran. */
    public int sign() {
        return sign;
    }
}
