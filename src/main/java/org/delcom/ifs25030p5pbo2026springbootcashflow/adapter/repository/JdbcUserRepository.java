package org.delcom.ifs25030p5pbo2026springbootcashflow.adapter.repository;

import org.delcom.ifs25030p5pbo2026springbootcashflow.domain.entity.Role;
import org.delcom.ifs25030p5pbo2026springbootcashflow.domain.entity.User;
import org.delcom.ifs25030p5pbo2026springbootcashflow.domain.repository.IUserRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Repository pengguna berbasis JDBC (PostgreSQL).
 */
public class JdbcUserRepository implements IUserRepository {
    private static final String COLUMNS = "id, username, password_hash, full_name, role, photo_path";

    private final DataSource dataSource;

    public JdbcUserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT " + COLUMNS + " FROM users ORDER BY id";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            List<User> users = new ArrayList<>();
            while (rs.next()) {
                users.add(mapRow(rs));
            }
            return users;
        } catch (SQLException ex) {
            throw new IllegalStateException("Gagal membaca daftar pengguna", ex);
        }
    }

    @Override
    public Optional<User> findById(Integer id) {
        String sql = "SELECT " + COLUMNS + " FROM users WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return readOptional(statement);
        } catch (SQLException ex) {
            throw new IllegalStateException("Gagal membaca pengguna", ex);
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT " + COLUMNS + " FROM users WHERE LOWER(username) = LOWER(?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            return readOptional(statement);
        } catch (SQLException ex) {
            throw new IllegalStateException("Gagal mencari pengguna", ex);
        }
    }

    @Override
    public User save(User entity) {
        return saveNew(entity.getUsername(), entity.getPasswordHash(), entity.getFullName(), entity.getRole());
    }

    @Override
    public User saveNew(String username, String passwordHash, String fullName, Role role) {
        String sql = "INSERT INTO users (username, password_hash, full_name, role, photo_path) VALUES (?, ?, ?, ?, NULL)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, username);
            statement.setString(2, passwordHash);
            statement.setString(3, fullName);
            statement.setString(4, role.name());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new IllegalStateException("Gagal mendapatkan ID pengguna baru");
                }
                return new User(keys.getInt(1), username, passwordHash, fullName, role, null);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Gagal menyimpan pengguna", ex);
        }
    }

    @Override
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new IllegalStateException("Gagal menghapus pengguna", ex);
        }
    }

    @Override
    public void update(User entity) {
        String sql = "UPDATE users SET password_hash = ?, full_name = ?, photo_path = ? WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, entity.getPasswordHash());
            statement.setString(2, entity.getFullName());
            statement.setString(3, entity.getPhotoPath());
            statement.setInt(4, entity.getId());
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Gagal mengubah pengguna", ex);
        }
    }

    @Override
    public List<User> findBy(Predicate<User> predicate) {
        return findAll().stream().filter(predicate).toList();
    }

    private static Optional<User> readOptional(PreparedStatement statement) throws SQLException {
        try (ResultSet rs = statement.executeQuery()) {
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
            return Optional.empty();
        }
    }

    private static User mapRow(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("full_name"),
                Role.valueOf(rs.getString("role")),
                rs.getString("photo_path")
        );
    }
}
