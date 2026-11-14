package org.delcom.ifs25030springbootcashflow.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordHasherTest {

    @Test
    void hashIsSha256Hex() {
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
                PasswordHasher.hash("abc"));
    }

    @Test
    void matchesComparesHash() {
        String hash = PasswordHasher.hash("rahasia");
        assertTrue(PasswordHasher.matches("rahasia", hash));
        assertFalse(PasswordHasher.matches("salah", hash));
    }

    @Test
    void unknownAlgorithmThrowsIllegalState() {
        assertThrows(IllegalStateException.class, () -> PasswordHasher.messageDigest("BUKAN-ALGORITMA"));
    }
}
