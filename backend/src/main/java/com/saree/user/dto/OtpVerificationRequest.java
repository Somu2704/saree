package com.saree.user.dto;

import jakarta.validation.constraints.NotBlank;

public record OtpVerificationRequest(
        @NotBlank(message = "Email is required") String email,
        @NotBlank(message = "OTP code is required") String otpCode) {
}
