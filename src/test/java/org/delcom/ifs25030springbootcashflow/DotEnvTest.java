package org.delcom.ifs25030p5pbo2026springbootcashflow;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;

class DotEnvTest {

    @AfterEach
    void cleanup() {
        for (String key : new String[]{"CF_A", "CF_B", "CF_C", "CF_D", "CF_E", "CF_F", "CF_KEEP", "CF_EMPTY"}) {
            System.clearProperty(key);
        }
    }

    @Test
    void loadsKeyValuesAndIgnoresInvalidLines(@TempDir Path dir) throws IOException {
        Path env = dir.resolve(".env");
        Files.writeString(env, String.join("\n",
                "# komentar",
                "",
                "CF_A=satu",
                "CF_B=\"dua spasi\"",
                "CF_C='tiga'",
                "tanpa-sama-dengan",
                "=tanpa-key",
                "CF_D=\"",
                "CF_E=\"tanpa-penutup",
                "CF_F='tanpa-penutup",
                "CF_EMPTY="));
        DotEnv.load(env);

        assertEquals("satu", System.getProperty("CF_A"));
        assertEquals("dua spasi", System.getProperty("CF_B"));
        assertEquals("tiga", System.getProperty("CF_C"));
        assertEquals("\"", System.getProperty("CF_D"));
        assertEquals("\"tanpa-penutup", System.getProperty("CF_E"));
        assertEquals("'tanpa-penutup", System.getProperty("CF_F"));
        assertEquals("", System.getProperty("CF_EMPTY"));
    }

    @Test
    void doesNotOverrideExistingProperty() {
        System.setProperty("CF_KEEP", "asli");
        DotEnv.applyLine("CF_KEEP=baru");
        assertEquals("asli", System.getProperty("CF_KEEP"));
    }

    @Test
    void doesNotOverrideEnvironmentVariable() {
        String existing = System.getenv().keySet().stream().findFirst().orElse(null);
        if (existing != null) {
            DotEnv.applyLine(existing + "=override");
            assertNull(System.getProperty(existing));
        }
    }

    @Test
    void missingFileIsIgnored(@TempDir Path dir) {
        assertDoesNotThrow(() -> DotEnv.load(dir.resolve("tidak-ada.env")));
    }

    @Test
    void loadDefaultPathDoesNotFail() {
        assertDoesNotThrow(() -> DotEnv.load());
    }
}
