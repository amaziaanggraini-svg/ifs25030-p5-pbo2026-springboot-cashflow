package org.delcom.ifs25030springbootcashflow.adapter.repository;

import org.delcom.ifs25030springbootcashflow.domain.entity.Role;
import org.delcom.ifs25030springbootcashflow.domain.entity.User;
import org.delcom.ifs25030springbootcashflow.domain.repository.IUserRepository;

import java.util.Optional;

/**
 * Implementasi in-memory untuk pengguna (unit test).
 */
public class UserRepository extends InMemoryRepository<User, Integer> implements IUserRepository {
    private int idCounter = 0;

    public UserRepository() {
        super(User::getId);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return findBy(user -> user.getUsername().equalsIgnoreCase(username)).stream().findFirst();
    }

    @Override
    public User saveNew(String username, String passwordHash, String fullName, Role role) {
        return save(new User(++idCounter, username, passwordHash, fullName, role, null));
    }
}
