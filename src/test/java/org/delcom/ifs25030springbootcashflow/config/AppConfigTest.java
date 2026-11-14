package org.delcom.ifs25030springbootcashflow.config;

import org.delcom.ifs25030springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25030springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25030springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25030springbootcashflow.usecase.AuthUseCase;
import org.delcom.ifs25030springbootcashflow.usecase.FinanceUseCase;
import org.delcom.ifs25030springbootcashflow.usecase.UserUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class AppConfigTest {

    @Autowired private IUserRepository userRepository;
    @Autowired private FinanceUseCase financeUseCase;
    @Autowired private AuthUseCase authUseCase;
    @Autowired private UserUseCase userUseCase;
    @Autowired private FinancePresenter presenter;
    @Autowired private FileStorage fileStorage;
    @Autowired private ApplicationRunner seedRootAdmin;
    @Autowired private WebMvcConfigurer uploadResourceConfigurer;

    @Test
    void beansAreWired() {
        assertNotNull(financeUseCase);
        assertNotNull(authUseCase);
        assertNotNull(userUseCase);
        assertNotNull(presenter);
    }

    @Test
    void rootAdminIsSeeded() throws Exception {
        seedRootAdmin.run(mock(ApplicationArguments.class));
        assertTrue(userRepository.findByUsername("admin").orElseThrow().isAdmin());
    }

    @Test
    void uploadResourceHandlerIsRegistered() {
        ResourceHandlerRegistry registry = mock(ResourceHandlerRegistry.class);
        org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration registration =
                mock(org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration.class);
        when(registry.addResourceHandler("/uploads/**")).thenReturn(registration);
        uploadResourceConfigurer.addResourceHandlers(registry);
        verify(registry).addResourceHandler("/uploads/**");
        verify(registration).addResourceLocations(anyString());
        assertNotNull(fileStorage.root());
    }
}
