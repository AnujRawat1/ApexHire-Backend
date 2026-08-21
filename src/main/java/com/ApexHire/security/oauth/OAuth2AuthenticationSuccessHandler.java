package com.ApexHire.security.oauth;

import com.ApexHire.user.model.User;
import com.ApexHire.user.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final OAuthAuthorizationCodeService authorizationCodeService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();

        String email = oauthUser.getAttribute("email");

        if (email == null || email.isBlank()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "OAuth email not found");
            return;
        }

        User user = userRepository
                .findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new IllegalStateException("OAuth user not found"));

        String code = authorizationCodeService.createCode(user);

        String redirectUrl = "http://localhost:5173/oauth/callback" + "?code=" + code;
        response.sendRedirect(redirectUrl);

    }
}