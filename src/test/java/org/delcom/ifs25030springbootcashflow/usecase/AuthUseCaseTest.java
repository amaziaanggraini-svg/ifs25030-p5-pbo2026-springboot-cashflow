package org.delcom.ifs25030p5pbo2026springbootcashflow.usecase;

import org.delcom.ifs25030p5pbo2026springbootcashflow.adapter.repository.UserRepository;
import org.delcom.ifs25030p5pbo2026springbootcashflow.domain.entity.Role;
import org.delcom.ifs25030p5pbo2026springbootcashflow.domain.entity.User;
import org.delcom.ifs25030p5pbo2026springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthUseCaseTest {
    private AuthUseCase auth;

    @BeforeEach
    void setUp() {
        auth = new AuthUseCase(new UserRepository());
    }

    @Test
    void registerHashesPasswordAndAssignsUserRole() throws Exception {
        User user = auth.register("  budi ", "rahasia", " Budi ");
        assertEquals("budi", user.getUsername());
        assertEquals("Budi", user.getFullName());
        assertEquals(Role.USER, user.getRole());
        assertNotEquals("rahasia", user.getPasswordHash());
    }

    @Test
    void registerValidations() throws Exception {
        assertThrows(ValidationException.class, () -> auth.register(null, "abcd", "N"));
        assertThrows(ValidationException.class, () -> auth.register("  ", "abcd", "N"));
        assertThrows(ValidationException.class, () -> auth.register("u", null, "N"));
        assertThrows(ValidationException.class, () -> auth.register("u", "abc", "N"));
        assertThrows(ValidationException.class, () -> auth.register("u", "abcd", null));
        assertThrows(ValidationException.class, () -> auth.register("u", "abcd", " "));
        auth.register("dup", "abcd", "Dup");
        assertThrows(ValidationException.class, () -> auth.register("DUP", "abcd", "Dup2"));
    }

    @Test
    void loginSuccessAndFailures() throws Exception {
        auth.register("budi", "rahasia", "Budi");
        assertEquals("budi", auth.login(" budi ", "rahasia").getUsername());
        assertThrows(ValidationException.class, () -> auth.login(null, "x"));
        assertThrows(ValidationException.class, () -> auth.login(" ", "x"));
        assertThrows(ValidationException.class, () -> auth.login("budi", null));
        assertThrows(ValidationException.class, () -> auth.login("budi", ""));
        assertThrows(ValidationException.class, () -> auth.login("hantu", "rahasia"));
        assertThrows(ValidationException.class, () -> auth.login("budi", "salah"));
    }

    @Test
    void ensureRootAdminSeedsOnceAndSyncsPassword() throws Exception {
        User first = auth.ensureRootAdmin("admin", "admin");
        assertTrue(first.isAdmin());
        // password sama: tidak ada perubahan
        User same = auth.ensureRootAdmin("admin", "admin");
        assertEquals(first.getId(), same.getId());
        assertEquals(first.getPasswordHash(), same.getPasswordHash());
        // password berbeda: akun tetap sama, password diperbarui
        User second = auth.ensureRootAdmin("admin", "lain");
        assertEquals(first.getId(), second.getId());
        assertEquals("admin", auth.login("admin", "lain").getUsername());
        assertThrows(ValidationException.class, () -> auth.login("admin", "admin"));
        assertThrows(ValidationException.class, () -> auth.ensureRootAdmin(null, "x"));
        assertThrows(ValidationException.class, () -> auth.ensureRootAdmin(" ", "x"));
        assertThrows(ValidationException.class, () -> auth.ensureRootAdmin("a", null));
        assertThrows(ValidationException.class, () -> auth.ensureRootAdmin("a", ""));
    }
}
