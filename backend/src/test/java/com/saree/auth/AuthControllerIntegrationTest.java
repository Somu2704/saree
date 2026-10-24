package com.saree.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.saree.user.dto.AuthResponse;
import com.saree.user.dto.LoginRequest;
import com.saree.user.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AuthControllerIntegrationTest {

    @Autowired
    private AuthService authService;

    @Test
    void registerAndLogin_shouldReturnJwtTokens() {
        RegisterRequest registerRequest = new RegisterRequest(
                "Nisha",
                "Kapoor",
                "nisha@example.com",
                "StrongPass123!",
                "+919999999999"
        );

        var user = authService.register(registerRequest);
        assertThat(user.email()).isEqualTo("nisha@example.com");

        AuthResponse authResponse = authService.login(new LoginRequest("nisha@example.com", "StrongPass123!"));

        assertThat(authResponse.accessToken()).isNotBlank();
        assertThat(authResponse.refreshToken()).isNotBlank();
        assertThat(authResponse.tokenType()).isEqualTo("Bearer");
    }
}
