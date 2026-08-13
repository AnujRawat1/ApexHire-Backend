package com.ApexHire.auth.service;

import com.ApexHire.auth.dto.AuthResponse;
import com.ApexHire.auth.dto.LoginRequestDto;
import com.ApexHire.auth.dto.SignUpRequestDto;
import com.ApexHire.security.authentication.CustomUserDetails;
import com.ApexHire.security.jwt.JwtService;
import com.ApexHire.token.RefreshToken;
import com.ApexHire.token.RefreshTokenService;
import com.ApexHire.user.model.AuthProvider;
import com.ApexHire.user.model.Role;
import com.ApexHire.user.model.User;
import com.ApexHire.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public void signUp(SignUpRequestDto request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Signup attempt with existing email: {}", request.getEmail());
            throw new RuntimeException("Email already registered");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(Role.USER))
                .provider(AuthProvider.EMAIL)
                .build();

        userRepository.save(user);
    }

    public AuthResponse login(LoginRequestDto request) {

        try {
            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    request.getEmail(),
                                    request.getPassword()
                            )
                    );
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            String accessToken  = jwtService.generateToken(userDetails);

            User user = userRepository.findByEmail(request.getEmail()).orElseThrow();

            String refreshToken = refreshTokenService.createRefreshToken(user);

            return new AuthResponse(accessToken, refreshToken);
        } catch (Exception e) {
            log.warn("Login attempt with invalid user: {}", request.getEmail());
            throw new RuntimeException("Invalid credentials");
        }
    }

    public AuthResponse refresh(String rawRefreshToken) {

        RefreshToken oldRefreshToken = refreshTokenService.validateRefreshToken(rawRefreshToken);

        User user = userRepository.findById(oldRefreshToken.getUserId())
                        .orElseThrow(() -> new RuntimeException("User not found"));

        CustomUserDetails userDetails = new CustomUserDetails(user);

        String newAccessToken = jwtService.generateToken(userDetails);

        String newRefreshToken = refreshTokenService.rotateRefreshToken(oldRefreshToken, user);

        return new AuthResponse(newAccessToken, newRefreshToken);
    }

    public void logout(String rawRefreshToken) {
        refreshTokenService.revokeRefreshToken(rawRefreshToken);
    }
}
