package com.saree.user.dto;

import jakarta.validation.constraints.NotBlank;

public record OtpRequest(@NotBlank(message = "Email is required") String email) {
}
