package org.delcom.ifs25030p5pbo2026springbootcashflow.usecase;

import org.delcom.ifs25030p5pbo2026springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25030p5pbo2026springbootcashflow.domain.entity.Balance;
import org.delcom.ifs25030p5pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25030p5pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25030p5pbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25030p5pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25030p5pbo2026springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FinanceUseCaseTest {
    private FinanceUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FinanceUseCase(new TransactionRepository());
    }

    @Test
    void addIncomeAndExpenseUpdateBalance() throws Exception {
        useCase.addIncome(1, "  Gaji ", " 5000000 ");
        useCase.addExpense(1, "Makan", "25000");
        useCase.addIncome(2, "Orang lain", "999");

        Balance balance = useCase.getBalance(1);
        assertEquals(2, balance.count());
        assertEquals(5_000_000, balance.totalIncome());
        assertEquals(25_000, balance.totalExpense());
        assertEquals(4_975_000, balance.balance());
        assertEquals("Gaji", useCase.getTransactionsByUser(1).get(0).getDescription());
    }

    @Test
    void addValidations() {
        assertThrows(ValidationException.class, () -> useCase.addIncome(1, null, "100"));
        assertThrows(ValidationException.class, () -> useCase.addIncome(1, "  ", "100"));
        assertThrows(ValidationException.class, () -> useCase.addIncome(1, "x", null));
        assertThrows(ValidationException.class, () -> useCase.addIncome(1, "x", " "));
        assertThrows(ValidationException.class, () -> useCase.addIncome(1, "x", "abc"));
        assertThrows(ValidationException.class, () -> useCase.addIncome(1, "x", "12.5"));
        assertThrows(ValidationException.class, () -> useCase.addIncome(1, "x", "0"));
        assertThrows(ValidationException.class, () -> useCase.addExpense(1, "x", "-5"));
        assertEquals(0, useCase.getBalance(1).count());
    }

    @Test
    void removeOwnTransaction() throws Exception {
        Transaction t = useCase.addIncome(1, "A", "100");
        useCase.removeTransactionForUser(1, t.getId());
        assertTrue(useCase.getTransactionsByUser(1).isEmpty());
    }

    @Test
    void removeValidations() throws Exception {
        Transaction t = useCase.addIncome(1, "A", "100");
        assertThrows(ValidationException.class, () -> useCase.removeTransactionForUser(1, 0));
        assertThrows(ValidationException.class, () -> useCase.removeTransactionForUser(1, -3));
        assertThrows(EntityNotFoundException.class, () -> useCase.removeTransactionForUser(1, 99));
        assertThrows(ValidationException.class, () -> useCase.removeTransactionForUser(2, t.getId()));
        assertEquals(1, useCase.getTransactionsByUser(1).size());
    }

    @Test
    void removeFailsWhenRepositoryCannotDelete() {
        TransactionRepository repo = new TransactionRepository() {
            @Override
            public boolean deleteById(Integer id) {
                return false;
            }
        };
        FinanceUseCase uc = new FinanceUseCase(repo);
        repo.save(1, "A", 10, TransactionType.INCOME);
        assertThrows(EntityNotFoundException.class, () -> uc.removeTransactionForUser(1, 1));
    }

    @Test
    void updateChangesOnlyGivenFields() throws Exception {
        Transaction t = useCase.addIncome(1, "A", "100");
        useCase.updateTransactionForUser(1, t.getId(), " B ", "250", TransactionType.EXPENSE, "p.png", false);
        assertEquals("B", t.getDescription());
        assertEquals(250, t.getAmount());
        assertEquals(TransactionType.EXPENSE, t.getType());
        assertEquals("p.png", t.getProofPath());

        useCase.updateTransactionForUser(1, t.getId(), null, null, null, null, false);
        assertEquals("B", t.getDescription());
        assertEquals("p.png", t.getProofPath());

        useCase.updateTransactionForUser(1, t.getId(), null, null, null, "ignored.png", true);
        assertNull(t.getProofPath());
    }

    @Test
    void updateValidations() throws Exception {
        Transaction t = useCase.addIncome(1, "A", "100");
        assertThrows(ValidationException.class,
                () -> useCase.updateTransactionForUser(1, t.getId(), " ", null, null, null, false));
        assertThrows(ValidationException.class,
                () -> useCase.updateTransactionForUser(1, t.getId(), null, "x", null, null, false));
        assertThrows(EntityNotFoundException.class,
                () -> useCase.updateTransactionForUser(1, 99, "B", null, null, null, false));
        assertThrows(ValidationException.class,
                () -> useCase.updateTransactionForUser(2, t.getId(), "B", null, null, null, false));
    }

    @Test
    void searchIsCaseInsensitiveAndScopedToUser() throws Exception {
        useCase.addIncome(1, "Gaji Bulanan", "100");
        useCase.addExpense(1, "Makan siang", "10");
        useCase.addIncome(2, "Gaji orang lain", "100");

        assertEquals(1, useCase.searchTransactions(1, "gAjI").size());
        assertEquals(2, useCase.searchTransactions(1, null).size());
        assertEquals(2, useCase.searchTransactions(1, "  ").size());
        assertTrue(useCase.searchTransactions(1, "tidak ada").isEmpty());
    }

    @Test
    void sortAndFind() throws Exception {
        useCase.addIncome(1, "A", "100");
        useCase.addExpense(1, "B", "300");
        useCase.addIncome(1, "C", "200");

        assertEquals(List.of("A", "C", "B"),
                useCase.sortTransactions(1, SortOption.AMOUNT_ASC).stream()
                        .map(Transaction::getDescription).toList());
        assertEquals("B", useCase.sortTransactions(1, SortOption.AMOUNT_DESC).get(0).getDescription());
        assertEquals("A", useCase.findTransactions(1, null, SortOption.AMOUNT_ASC).get(0).getDescription());
        assertEquals("B", useCase.findTransactions(1, "", SortOption.EXPENSE_FIRST).get(0).getDescription());
        assertEquals(3, useCase.findTransactions(1, null, null).size());
        assertEquals("C", useCase.findTransactions(1, "c", SortOption.AMOUNT_DESC).get(0).getDescription());
    }

    @Test
    void parseAmountEdgeCases() throws Exception {
        assertEquals(1, FinanceUseCase.parseAmount("1"));
        assertThrows(ValidationException.class, () -> FinanceUseCase.parseAmount("99999999999999999999"));
    }
}
