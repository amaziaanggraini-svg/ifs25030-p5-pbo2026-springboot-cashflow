package org.delcom.ifs25030springbootcashflow.adapter.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileStorageTest {

    @TempDir
    Path dir;

    @Test
    void storesFileAndReturnsRelativePath() throws Exception {
        FileStorage storage = new FileStorage(dir.toString());
        String rel = storage.store("proofs", "Struk.PNG", new ByteArrayInputStream("data".getBytes()));

        assertTrue(rel.startsWith("proofs/"));
        assertTrue(rel.endsWith(".png"));
        assertEquals("data", Files.readString(storage.resolve(rel)));
        assertEquals(dir.toAbsolutePath().normalize(), storage.root());
    }

    @Test
    void fallsBackToBinExtension() throws Exception {
        FileStorage storage = new FileStorage(dir.toString());
        assertTrue(storage.store("p", null, new ByteArrayInputStream(new byte[0])).endsWith(".bin"));
        assertTrue(storage.store("p", "noext", new ByteArrayInputStream(new byte[0])).endsWith(".bin"));
        assertTrue(storage.store("p", "a.terlalupanjangekstensi", new ByteArrayInputStream(new byte[0]))
                .endsWith(".bin"));
    }
}
