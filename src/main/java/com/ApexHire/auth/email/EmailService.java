package com.ApexHire.auth.email;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendVerificationCode(String recipientEmail, String verificationCode) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(System.getenv("MAIL_FROM"));
            helper.setTo(recipientEmail);
            helper.setSubject("ApexHire Email Verification");

            String htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; background-color: #f5f5f5; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #6B46C1; color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                        .header h1 { margin: 0; font-size: 28px; }
                        .content { background: white; padding: 30px; border-radius: 0 0 10px 10px; }
                        .code { background-color: #E9D8FD; color: #6B46C1; padding: 20px; font-size: 28px; font-weight: bold; text-align: center; border-radius: 8px; margin: 25px 0; letter-spacing: 4px; border: 2px solid #6B46C1; }
                        .footer { margin-top: 20px; color: #666; font-size: 12px; text-align: center; }
                        .button { display: inline-block; background-color: #6B46C1; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin-top: 20px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Welcome to ApexHire</h1>
                        </div>
                        <div class="content">
                            <p>Hello,</p>
                            <p>Thank you for signing up with ApexHire. Please use the verification code below to complete your registration:</p>
                            <div class="code">%s</div>
                            <p><strong>This code will expire in 10 minutes.</strong></p>
                            <p>If you did not create an ApexHire account, you can safely ignore this email.</p>
                            <p>Regards,<br>The ApexHire Team</p>
                        </div>
                        <div class="footer">
                            <p>&copy; 2024 ApexHire. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(verificationCode);

            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send verification email", e);
        }
    }
}