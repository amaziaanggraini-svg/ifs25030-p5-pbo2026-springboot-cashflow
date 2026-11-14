package org.delcom.ifs25030p5pbo2026springbootcashflow.adapter.repository;

import org.delcom.ifs25030p5pbo2026springbootcashflow.domain.entity.Role;
import org.delcom.ifs25030p5pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25030p5pbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25030p5pbo2026springbootcashflow.domain.entity.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RepositoryTest {

    @Test
    void transactionRepositoryCrud() {
        TransactionRepository repo = new TransactionRepository();
        Transaction a = repo.save(1, "A", 100, TransactionType.INCOME);
        Transaction b = repo.save(2, "B", 50, TransactionType.EXPENSE);
        assertEquals(1, a.getId());
        assertEquals(2, b.getId());
        assertEquals(2, repo.findAll().size());
        assertEquals(1, repo.findByUserId(1).size());
        assertTrue(repo.findById(2).isPresent());
        assertTrue(repo.findById(9).isEmpty());
        assertEquals(1, repo.findBy(t -> t.getAmount() > 60).size());

        b.changeDescription("B2");
        repo.update(b);
        assertEquals("B2", repo.findById(2).orElseThrow().getDescription());

        assertTrue(repo.deleteById(1));
        assertFalse(repo.deleteById(1));
        assertEquals(1, repo.findAll().size());
    }

    @Test
    void findAllReturnsDefensiveCopy() {
        TransactionRepository repo = new TransactionRepository();
        repo.save(1, "A", 100, TransactionType.INCOME);
        repo.findAll().clear();
        assertEquals(1, repo.findAll().size());
    }

    @Test
    void userRepository() {
        UserRepository repo = new UserRepository();
        User u = repo.saveNew("Budi", "h", "Budi", Role.USER);
        assertEquals(1, u.getId());
        assertTrue(repo.findByUsername("budi").isPresent());
        assertTrue(repo.findByUsername("none").isEmpty());
        assertEquals(1, repo.findAll().size());
    }
}
