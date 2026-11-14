package org.delcom.ifs25030springbootcashflow;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

/**
 * Smoke test: konteks Spring (AppConfig + H2 lewat profil test) harus bisa dibuat.
 */
@SpringBootTest
@ActiveProfiles("test")
class AppTest {

    @Test
    void contextLoads() {
        // Jika wiring AppConfig / schema H2 salah, test ini gagal saat konteks dibuat.
    }

    @Test
    void mainDelegatesToSpringApplication() throws Exception {
        try (MockedStatic<SpringApplication> spring = mockStatic(SpringApplication.class)) {
            App.main(new String[0]);
            spring.verify(() -> SpringApplication.run(eq(App.class), any(String[].class)));
        }
    }
}
