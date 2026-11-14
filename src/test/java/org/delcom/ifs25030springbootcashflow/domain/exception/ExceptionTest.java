package org.delcom.ifs25030p5pbo2026springbootcashflow.domain.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionTest {

    @Test
    void domainExceptionHierarchy() {
        RuntimeException cause = new RuntimeException("akar");
        DomainException withCause = new DomainException("pesan", cause);
        assertEquals("pesan", withCause.getMessage());
        assertSame(cause, withCause.getCause());
        assertEquals("x", new DomainException("x").getMessage());

        EntityNotFoundException notFound = new EntityNotFoundException("Transaksi", 7);
        assertEquals(7, notFound.getId());
        assertEquals("Transaksi dengan ID 7 tidak ditemukan.", notFound.getMessage());
        assertInstanceOf(DomainException.class, notFound);

        ValidationException validation = new ValidationException("salah");
        assertEquals("salah", validation.getMessage());
        assertInstanceOf(DomainException.class, validation);
    }
}
