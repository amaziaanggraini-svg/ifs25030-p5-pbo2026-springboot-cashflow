package org.delcom.ifs25030p5pbo2026springbootcashflow.usecase;

import org.delcom.ifs25030p5pbo2026springbootcashflow.adapter.repository.UserRepository;
import org.delcom.ifs25030p5pbo2026springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25030p5pbo2026springbootcashflow.domain.entity.Role;
import org.delcom.ifs25030p5pbo2026springbootcashflow.domain.entity.User;
import org.delcom.ifs25030p5pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25030p5pbo2026springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserUseCaseTest {
    private UserRepository repository;
    private UserUseCase useCase;
    private User user;

    @BeforeEach
    void setUp() {
        repository = new UserRepository();
        useCase = new UserUseCase(repository);
        user = repository.saveNew("budi", PasswordHasher.hash("lama"), "Budi", Role.USER);
    }

    @Test
    void listAndGet() throws Exception {
        assertEquals(1, useCase.listUsers().size());
        assertEquals("budi", useCase.getUser(user.getId()).getUsername());
        assertThrows(EntityNotFoundException.class, () -> useCase.getUser(99));
    }

    @Test
    void updateProfileNameOnlyKeepsPassword() throws Exception {
        String before = user.getPasswordHash();
        useCase.updateProfile(user.getId(), " Budi Baru ", null);
        assertEquals("Budi Baru", user.getFullName());
        assertEquals(before, user.getPasswordHash());
        useCase.updateProfile(user.getId(), "Budi Baru", "  ");
        assertEquals(before, user.getPasswordHash());
    }

    @Test
    void updateProfileChangesPassword() throws Exception {
        useCase.updateProfile(user.getId(), "Budi", "barubaru");
        assertTrue(PasswordHasher.matches("barubaru", user.getPasswordHash()));
    }

    @Test
    void updateProfileValidations() {
        assertThrows(EntityNotFoundException.class, () -> useCase.updateProfile(99, "x", null));
        assertThrows(ValidationException.class, () -> useCase.updateProfile(user.getId(), null, null));
        assertThrows(ValidationException.class, () -> useCase.updateProfile(user.getId(), " ", null));
        assertThrows(ValidationException.class, () -> useCase.updateProfile(user.getId(), "Budi", "abc"));
    }

    @Test
    void updatePhoto() throws Exception {
        useCase.updatePhoto(user.getId(), "profiles/a.png");
        assertEquals("profiles/a.png", user.getPhotoPath());
        assertThrows(ValidationException.class, () -> useCase.updatePhoto(user.getId(), null));
        assertThrows(ValidationException.class, () -> useCase.updatePhoto(user.getId(), " "));
        assertThrows(EntityNotFoundException.class, () -> useCase.updatePhoto(99, "x.png"));
    }
}
