package com.ApexHire.auth.verification;

import com.ApexHire.auth.email.EmailService;
import com.ApexHire.user.model.AuthProvider;
import com.ApexHire.user.model.Role;
import com.ApexHire.user.model.User;
import com.ApexHire.user.repository.UserRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {

    private final PendingSignupRepository pendingSignupRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationCodeGenerator codeGenerator;
    private final EmailService emailService;

    public void verifyEmail(String email, String code) {

        PendingSignup pendingSignup = pendingSignupRepository
                        .findByEmail(email)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "No pending signup found"
                                ));

        // Check expiration
        if (pendingSignup.getExpiresAt().isBefore(Instant.now())) {
            pendingSignupRepository.delete(pendingSignup);
            throw new RuntimeException("Verification code has expired");
        }

        // Check verification code
        if (!passwordEncoder.matches(code, pendingSignup.getCodeHash())) {
            throw new RuntimeException("Invalid verification code");
        }

        // Create actual user
        User user = User.builder()
                .name(pendingSignup.getName())
                .email(pendingSignup.getEmail())
                .password(pendingSignup.getPasswordHash())
                .passwordSet(true)
                .provider(AuthProvider.EMAIL)
                .emailVerified(true)
                .roles(Set.of(Role.USER))
                .build();

        userRepository.save(user);

        // Delete temporary signup
        pendingSignupRepository.delete(pendingSignup);

        log.info("Email verified successfully for {}", email);
    }

    public void createPendingSignup(String name, String email, String password) {

        // Remove previous pending signup for this email
        pendingSignupRepository.deleteByEmail(email);

        String code = codeGenerator.generate();

        PendingSignup pendingSignup = PendingSignup.builder()
                .name(name)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .codeHash(passwordEncoder.encode(code))
                .createdAt(Instant.now())
                .expiresAt(
                        Instant.now().plusSeconds(10 * 60)
                )
                .build();

        pendingSignupRepository.save(pendingSignup);

        emailService.sendVerificationCode(email, code);
        log.info("Verification Code Sent : {} | Email : {}", code, email);
    }
}