package org.delcom.ifs25030springbootcashflow.adapter.repository;

import org.delcom.ifs25030springbootcashflow.domain.entity.Role;
import org.delcom.ifs25030springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25030springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25030springbootcashflow.domain.entity.User;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JdbcRepositoryTest {
    private JdbcUserRepository users;
    private JdbcTransactionRepository transactions;
    private User owner;

    @BeforeEach
    void setUp() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:" + UUID.randomUUID()
                + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
        ds.setUser("sa");
        DataSource dataSource = ds;
        SchemaInitializer.initialize(dataSource);
        // initialize() aman dipanggil berulang (IF NOT EXISTS)
        SchemaInitializer.initialize(dataSource);
        users = new JdbcUserRepository(dataSource);
        transactions = new JdbcTransactionRepository(dataSource);
        owner = users.saveNew("budi", "hash", "Budi", Role.USER);
    }

    @Test
    void userCrud() {
        assertTrue(owner.getId() > 0);
        assertEquals("budi", users.findById(owner.getId()).orElseThrow().getUsername());
        assertTrue(users.findById(999).isEmpty());
        assertEquals("Budi", users.findByUsername("BUDI").orElseThrow().getFullName());
        assertTrue(users.findByUsername("hantu").isEmpty());

        owner.changeFullName("Budi S");
        owner.changePasswordHash("h2");
        owner.changePhotoPath("profiles/a.png");
        users.update(owner);
        User reloaded = users.findById(owner.getId()).orElseThrow();
        assertEquals("Budi S", reloaded.getFullName());
        assertEquals("h2", reloaded.getPasswordHash());
        assertEquals("profiles/a.png", reloaded.getPhotoPath());
        assertEquals(Role.USER, reloaded.getRole());

        User saved = users.save(new User(0, "ani", "h", "Ani", Role.ADMIN, null));
        assertTrue(saved.isAdmin());
        assertEquals(2, users.findAll().size());
        assertEquals(1, users.findBy(User::isAdmin).size());
        assertTrue(users.deleteById(saved.getId()));
        assertFalse(users.deleteById(saved.getId()));
    }

    @Test
    void transactionCrud() {
        Transaction created = transactions.save(owner.getId(), "Gaji", 5_000_000L, TransactionType.INCOME);
        assertTrue(created.getId() > 0);
        assertNull(created.getProofPath());

        Transaction loaded = transactions.findById(created.getId()).orElseThrow();
        assertEquals("Gaji", loaded.getDescription());
        assertEquals(5_000_000L, loaded.getAmount());
        assertEquals(TransactionType.INCOME, loaded.getType());
        assertNotNull(loaded.getCreatedAt());
        assertTrue(transactions.findById(999).isEmpty());

        loaded.changeDescription("Gaji Naik");
        loaded.changeAmount(6_000_000L);
        loaded.changeType(TransactionType.EXPENSE);
        loaded.changeProofPath("proofs/x.png");
        transactions.update(loaded);
        Transaction updated = transactions.findById(created.getId()).orElseThrow();
        assertEquals("Gaji Naik", updated.getDescription());
        assertEquals(6_000_000L, updated.getAmount());
        assertEquals(TransactionType.EXPENSE, updated.getType());
        assertEquals("proofs/x.png", updated.getProofPath());

        assertEquals(1, transactions.findAll().size());
        assertEquals(1, transactions.findByUserId(owner.getId()).size());
        assertTrue(transactions.findByUserId(999).isEmpty());
        assertEquals(1, transactions.findBy(t -> t.getAmount() > 1).size());

        assertTrue(transactions.deleteById(created.getId()));
        assertFalse(transactions.deleteById(created.getId()));
    }

    @Test
    void saveEntityWithAndWithoutProof() {
        Transaction plain = transactions.save(
                new Transaction(0, owner.getId(), "Plain", 10, TransactionType.INCOME));
        assertNull(plain.getProofPath());

        Transaction withProof = transactions.save(new Transaction(
                0, owner.getId(), "Bukti", 20, TransactionType.EXPENSE, "proofs/b.png", LocalDateTime.now()));
        assertEquals("proofs/b.png", transactions.findById(withProof.getId()).orElseThrow().getProofPath());
    }
}
