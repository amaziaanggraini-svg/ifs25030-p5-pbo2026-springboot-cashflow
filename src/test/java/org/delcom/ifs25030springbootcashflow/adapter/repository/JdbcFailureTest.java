package org.delcom.ifs25030p5pbo2026springbootcashflow.adapter.repository;

import org.delcom.ifs25030p5pbo2026springbootcashflow.domain.entity.Role;
import org.delcom.ifs25030p5pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25030p5pbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25030p5pbo2026springbootcashflow.domain.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JdbcFailureTest {
    private DataSource broken;
    private DataSource noKeys;

    @BeforeEach
    void setUp() throws SQLException {
        broken = mock(DataSource.class);
        when(broken.getConnection()).thenThrow(new SQLException("down"));

        ResultSet emptyKeys = mock(ResultSet.class);
        when(emptyKeys.next()).thenReturn(false);
        PreparedStatement statement = mock(PreparedStatement.class);
        when(statement.getGeneratedKeys()).thenReturn(emptyKeys);
        Connection connection = mock(Connection.class);
        when(connection.prepareStatement(anyString(), anyInt())).thenReturn(statement);
        noKeys = mock(DataSource.class);
        when(noKeys.getConnection()).thenReturn(connection);
    }

    @Test
    void transactionRepositoryWrapsSqlExceptions() {
        JdbcTransactionRepository repo = new JdbcTransactionRepository(broken);
        Transaction t = new Transaction(1, 1, "x", 1, TransactionType.INCOME);
        assertThrows(IllegalStateException.class, repo::findAll);
        assertThrows(IllegalStateException.class, () -> repo.findByUserId(1));
        assertThrows(IllegalStateException.class, () -> repo.findById(1));
        assertThrows(IllegalStateException.class, () -> repo.save(1, "x", 1, TransactionType.INCOME));
        assertThrows(IllegalStateException.class, () -> repo.deleteById(1));
        assertThrows(IllegalStateException.class, () -> repo.update(t));
    }

    @Test
    void userRepositoryWrapsSqlExceptions() {
        JdbcUserRepository repo = new JdbcUserRepository(broken);
        User u = new User(1, "a", "h", "A", Role.USER, null);
        assertThrows(IllegalStateException.class, repo::findAll);
        assertThrows(IllegalStateException.class, () -> repo.findById(1));
        assertThrows(IllegalStateException.class, () -> repo.findByUsername("a"));
        assertThrows(IllegalStateException.class, () -> repo.saveNew("a", "h", "A", Role.USER));
        assertThrows(IllegalStateException.class, () -> repo.deleteById(1));
        assertThrows(IllegalStateException.class, () -> repo.update(u));
    }

    @Test
    void schemaInitializerWrapsSqlException() {
        assertThrows(IllegalStateException.class, () -> SchemaInitializer.initialize(broken));
    }

    @Test
    void saveFailsWhenNoGeneratedKey() {
        assertThrows(IllegalStateException.class,
                () -> new JdbcTransactionRepository(noKeys).save(1, "x", 1, TransactionType.INCOME));
        assertThrows(IllegalStateException.class,
                () -> new JdbcUserRepository(noKeys).saveNew("a", "h", "A", Role.USER));
    }
}
