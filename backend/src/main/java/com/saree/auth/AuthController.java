package com.saree.auth;

import com.saree.user.dto.AuthResponse;
import com.saree.user.dto.LoginRequest;
import com.saree.user.dto.OtpRequest;
import com.saree.user.dto.OtpVerificationRequest;
import com.saree.user.dto.RefreshRequest;
import com.saree.user.dto.RegisterRequest;
import com.saree.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User registration, login, token refresh and OTP flows")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new customer account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation or duplicate user error"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and create tokens")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "400", description = "Bad request payload")
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tokens refreshed"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request.refreshToken()));
    }

    @PostMapping("/otp/send")
    @Operation(summary = "Send OTP for a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP generated successfully"),
            @ApiResponse(responseCode = "400", description = "User not found or invalid payload")
    })
    public ResponseEntity<Map<String, Object>> sendOtp(@Valid @RequestBody OtpRequest request) {
        return ResponseEntity.ok(authService.sendOtp(request.email()));
    }

    @PostMapping("/otp/verify")
    @Operation(summary = "Verify OTP code")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP verification result returned"),
            @ApiResponse(responseCode = "400", description = "Invalid payload")
    })
    public ResponseEntity<Map<String, Boolean>> verifyOtp(@Valid @RequestBody OtpVerificationRequest request) {
        return ResponseEntity.ok(Map.of("verified", authService.verifyOtp(request.email(), request.otpCode())));
    }
}
