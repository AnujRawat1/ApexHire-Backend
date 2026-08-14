package com.ApexHire.auth.controller;

import com.ApexHire.auth.dto.*;
import com.ApexHire.auth.service.AuthService;
import com.ApexHire.auth.verification.EmailVerificationService;
import com.ApexHire.security.authentication.CustomUserDetails;
import com.ApexHire.security.authentication.CustomUserDetailsService;
import com.ApexHire.security.jwt.JwtService;
import com.ApexHire.security.oauth.OAuthAuthorizationCodeService;
import com.ApexHire.token.RefreshTokenService;
import com.ApexHire.user.model.User;
import com.ApexHire.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(
        name = "Authentication",
        description = "User authentication and account management APIs"
)
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    private final OAuthAuthorizationCodeService authorizationCodeService;
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;


    @Operation(
            summary = "Login",
            description = "Authenticate a user using email and password"
    )
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequestDto request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sign-up")
    public ResponseEntity<String> signup(@Valid @RequestBody SignUpRequestDto request) {

        authService.signUp(request);
        return ResponseEntity.ok("Verification code sent to your email");
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        AuthResponse response = authService.refresh(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@Valid @RequestBody LogoutRequest request) {

        authService.logout(request.getRefreshToken());

        return ResponseEntity.ok(
                Map.of("message", "Logged out successfully")
        );
    }

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        User user = emailVerificationService.verifyEmail(request.getEmail(), request.getCode());

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getEmail());

        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = refreshTokenService.createRefreshToken(user);

        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {

        emailVerificationService.resendVerificationCode(request.getEmail());
        return ResponseEntity.ok("Verification code resent successfully");

    }

    @PostMapping("/oauth/exchange")
    public ResponseEntity<OAuthTokenResponse> exchangeOAuthCode(@RequestBody OAuthCodeRequest request) {

        String userId = authorizationCodeService.consumeCode(request.getCode());

        User user = userRepository.findById(userId)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        CustomUserDetails userDetails = new CustomUserDetails(user);

        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = refreshTokenService.createRefreshToken(user);

        return ResponseEntity.ok(
                new OAuthTokenResponse(accessToken, refreshToken)
        );
    }

    @Operation(
            summary = "Set OAuth password",
            description = "Allows an authenticated Google/GitHub user to set an email-login password"
    )
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/set-password")
    public ResponseEntity<?> setPassword(@Valid @RequestBody SetPasswordRequest request, Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (user.isPasswordSet()) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Password is already set")
            );
        }

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPasswordSet(true);

        userRepository.save(user);

        return ResponseEntity.ok(
                Map.of(
                        "message", "Password set successfully"
                )
        );
    }
}
