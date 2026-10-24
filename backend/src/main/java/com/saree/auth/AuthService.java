package com.saree.auth;

import com.saree.common.security.JwtTokenProvider;
import com.saree.user.dto.AuthResponse;
import com.saree.user.dto.LoginRequest;
import com.saree.user.dto.RegisterRequest;
import com.saree.user.dto.UserResponse;
import com.saree.user.entity.Role;
import com.saree.user.entity.User;
import com.saree.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final Random random = new Random();

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public UserResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("User already exists with email: " + email);
        }

        User user = new User();
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setPhoneNumber(request.phoneNumber().trim());
        user.setRole(Role.CUSTOMER);

        userRepository.save(user);

        return new UserResponse(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        return toAuthResponse(user);
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        Claims claims = jwtTokenProvider.parseClaims(refreshToken);
        User user = userRepository.findByEmail(claims.getSubject())
                .orElseThrow(() -> new IllegalArgumentException("User no longer exists"));

        return toAuthResponse(user);
    }

    public Map<String, Object> sendOtp(String email) {
        String normalizedEmail = normalizeEmail(email);
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String otpCode = String.format("%06d", random.nextInt(1_000_000));
        user.setOtpCode(otpCode);
        user.setOtpExpiresAt(Instant.now().plusSeconds(300));
        userRepository.save(user);

        return Map.of(
                "message", "OTP sent successfully",
                "otpCode", otpCode,
                "expiresInSeconds", 300
        );
    }

    public boolean verifyOtp(String email, String otpCode) {
        String normalizedEmail = normalizeEmail(email);
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getOtpCode() == null || user.getOtpExpiresAt() == null || user.getOtpExpiresAt().isBefore(Instant.now())) {
            return false;
        }

        boolean matches = user.getOtpCode().equals(otpCode);
        if (matches) {
            user.setOtpCode(null);
            user.setOtpExpiresAt(null);
            userRepository.save(user);
        }

        return matches;
    }

    private AuthResponse toAuthResponse(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);
        return new AuthResponse(accessToken, refreshToken, "Bearer", user.getEmail(), user.getRole().name(), user.getId());
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
