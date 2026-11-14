package org.delcom.ifs25030p5pbo2026springbootcashflow.domain.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EntityTest {

    private static Transaction tx(int id, long amount, TransactionType type) {
        return new Transaction(id, 1, "t" + id, amount, type);
    }

    @Test
    void transactionDefaultsAndMutators() {
        Transaction t = tx(1, 500, TransactionType.INCOME);
        assertEquals(1, t.getId());
        assertEquals(1, t.getUserId());
        assertEquals("t1", t.getDescription());
        assertEquals(500, t.getAmount());
        assertEquals(TransactionType.INCOME, t.getType());
        assertNull(t.getProofPath());
        assertNotNull(t.getCreatedAt());
        assertTrue(t.isIncome());
        assertEquals(500, t.signedAmount());

        t.changeDescription("baru");
        t.changeAmount(200);
        t.changeType(TransactionType.EXPENSE);
        t.changeProofPath("proofs/a.png");
        assertEquals("baru", t.getDescription());
        assertEquals(200, t.getAmount());
        assertFalse(t.isIncome());
        assertEquals(-200, t.signedAmount());
        assertEquals("proofs/a.png", t.getProofPath());
    }

    @Test
    void transactionFullConstructor() {
        LocalDateTime now = LocalDateTime.of(2026, 1, 2, 3, 4);
        Transaction t = new Transaction(9, 2, "x", 10, TransactionType.EXPENSE, "p", now);
        assertEquals(now, t.getCreatedAt());
        assertEquals("p", t.getProofPath());
    }

    @Test
    void typeLabelAndSign() {
        assertEquals("Pemasukan", TransactionType.INCOME.label());
        assertEquals("Pengeluaran", TransactionType.EXPENSE.label());
        assertEquals(1, TransactionType.INCOME.sign());
        assertEquals(-1, TransactionType.EXPENSE.sign());
    }

    @Test
    void balanceSummarizes() {
        Balance b = Balance.of(List.of(
                tx(1, 1000, TransactionType.INCOME),
                tx(2, 300, TransactionType.EXPENSE),
                tx(3, 200, TransactionType.INCOME)));
        assertEquals(3, b.count());
        assertEquals(1200, b.totalIncome());
        assertEquals(300, b.totalExpense());
        assertEquals(900, b.balance());
        assertEquals(0, Balance.of(List.of()).balance());
    }

    @Test
    void sortOptionsOrderCorrectly() {
        List<Transaction> data = List.of(
                tx(1, 300, TransactionType.EXPENSE),
                tx(2, 100, TransactionType.INCOME),
                tx(3, 200, TransactionType.EXPENSE));

        assertEquals(List.of(2, 3, 1), ids(data, SortOption.AMOUNT_ASC));
        assertEquals(List.of(1, 3, 2), ids(data, SortOption.AMOUNT_DESC));
        assertEquals(2, ids(data, SortOption.INCOME_FIRST).get(0));
        assertEquals(2, ids(data, SortOption.EXPENSE_FIRST).get(2));
        assertEquals(4, SortOption.values().length);
        for (SortOption option : SortOption.values()) {
            assertNotNull(option.comparator());
        }
    }

    private static List<Integer> ids(List<Transaction> data, SortOption option) {
        List<Transaction> copy = new ArrayList<>(data);
        copy.sort(option.comparator());
        return copy.stream().map(Transaction::getId).toList();
    }

    @Test
    void userAccessorsAndMutators() {
        User u = new User(1, "budi", "hash", "Budi", Role.USER, null);
        assertEquals(1, u.getId());
        assertEquals("budi", u.getUsername());
        assertEquals("hash", u.getPasswordHash());
        assertEquals("Budi", u.getFullName());
        assertEquals(Role.USER, u.getRole());
        assertNull(u.getPhotoPath());
        assertFalse(u.isAdmin());

        u.changeFullName("Budi S");
        u.changePasswordHash("h2");
        u.changePhotoPath("profiles/x.png");
        assertEquals("Budi S", u.getFullName());
        assertEquals("h2", u.getPasswordHash());
        assertEquals("profiles/x.png", u.getPhotoPath());
        assertTrue(new User(2, "a", "h", "A", Role.ADMIN, null).isAdmin());
        assertEquals(2, Role.values().length);
        assertEquals(Role.ADMIN, Role.valueOf("ADMIN"));
        assertEquals(TransactionType.INCOME, TransactionType.valueOf("INCOME"));
        assertEquals(SortOption.AMOUNT_ASC, SortOption.valueOf("AMOUNT_ASC"));
        assertNotNull(Comparator.<Integer>naturalOrder());
    }
}
