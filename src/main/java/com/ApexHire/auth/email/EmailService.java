package com.ApexHire.auth.email;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendVerificationCode(String recipientEmail, String verificationCode) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(System.getenv("MAIL_FROM"));
        message.setTo(recipientEmail);
        message.setSubject("ApexHire Email Verification");

        message.setText(
                """
                Hello,

                Your ApexHire verification code is:

                %s

                This code will expire in 10 minutes.

                If you did not create an ApexHire account, you can ignore this email.

                Regards,
                ApexHire Team
                """.formatted(verificationCode)
        );

        mailSender.send(message);
    }
}