package com.saree.user.dto;

import java.util.UUID;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        String email,
        String role,
        UUID userId) {
}
