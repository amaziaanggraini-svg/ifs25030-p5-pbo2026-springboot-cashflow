# Cashflow — ifs25030-p5-pbo2026-springboot-cashflow

Aplikasi web pencatat keuangan pribadi (Clean Architecture, Java Generics, Java Exceptions,
Spring Boot, Vaadin, JDBC PostgreSQL, autentikasi, profil, dan role).

## Menjalankan

1. Buat database PostgreSQL:
   ```sql
   CREATE DATABASE pbo2026_cashflow;
   ```
2. Salin `.env.example` menjadi `.env`, lalu isi `DB_*`, `ROOT_*`, `APP_PORT`, `UPLOAD_DIR`.
3. Jalankan:
   ```
   .\mvnw.cmd spring-boot:run        (Windows)
   ./mvnw spring-boot:run            (Linux/macOS)
   ```
4. Buka `http://localhost:8080` — login dengan `ROOT_USERNAME` / `ROOT_PASSWORD` (admin) atau daftar user baru.

Tabel `users` dan `transactions` dibuat otomatis (`SchemaInitializer`), admin di-seed otomatis.

## Tes

```
.\mvnw.cmd clean verify      # unit test (H2) + Jacoco 100% (kecuali framework/view & framework/auth)
```

## Rute

| Path | View | Akses |
|---|---|---|
| `/login`, `/register` | LoginView, RegisterView | Publik (sudah login → `/`) |
| `/` | FinanceView | USER & ADMIN |
| `/profile` | ProfileView | USER & ADMIN |
| `/users` | UsersView | ADMIN |
| `/user-transactions/{id}` | UserTransactionsView (read-only) | ADMIN |
| `/uploads/**` | FileStorage (bukti & foto profil) | Publik (static) |
