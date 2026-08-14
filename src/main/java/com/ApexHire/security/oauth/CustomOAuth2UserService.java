package com.ApexHire.security.oauth;

import com.ApexHire.user.model.AuthProvider;
import com.ApexHire.user.model.Role;
import com.ApexHire.user.model.User;
import com.ApexHire.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final GithubEmailService githubEmailService;
    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // Get user information from Google/GitHub
        OAuth2User oauthUser = delegate.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId();

        String email = oauthUser.getAttribute("email");

        if (email == null || email.isBlank()) {
            if (provider.equalsIgnoreCase("github")) {
                email = githubEmailService.getPrimaryVerifiedEmail(userRequest.getAccessToken().getTokenValue());
            }
        }

        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException("OAuth provider did not provide an email");
        }
        String finalEmail = email.toLowerCase().trim();

        String name = oauthUser.getAttribute("name");
        String finalName = (name == null || name.isBlank()) ? finalEmail : name;

        // Find existing user
        User user = userRepository
                .findByEmail(finalEmail)
                .orElseGet(() -> createOAuthUser(finalName, finalEmail, provider));

        /*
         * Existing EMAIL account:
         *
         * Do NOT automatically merge it with Google/GitHub.
         */
        if (user.getProvider() == AuthProvider.EMAIL) {
            throw new OAuth2AuthenticationException("An account already exists with this email. " + "Please login using email/password.");
        }

        // Existing OAuth account
        if (!user.getProvider().name().equalsIgnoreCase(provider)) {
            throw new OAuth2AuthenticationException("This email is already linked to another OAuth provider.");
        }

        Map<String, Object> attributes = new HashMap<>(oauthUser.getAttributes());

        attributes.put("email", finalEmail);

        return new DefaultOAuth2User(
                Set.of(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                getNameAttribute(provider)
        );
    }

    private User createOAuthUser(String name, String email, String provider) {

        AuthProvider authProvider;

        if (provider.equalsIgnoreCase("google")) {
            authProvider = AuthProvider.GOOGLE;
        } else if (provider.equalsIgnoreCase("github")) {
            authProvider = AuthProvider.GITHUB;
        } else {
            throw new OAuth2AuthenticationException(
                    "Unsupported OAuth provider"
            );
        }

        User user = User.builder()
                .name(name)
                .email(email)
                .password(null)
                .passwordSet(false)
                .provider(authProvider)
                .emailVerified(true)
                .roles(Set.of(Role.USER))
                .build();

        return userRepository.save(user);
    }

    private String getNameAttribute(String provider) {

        if (provider.equalsIgnoreCase("github"))
            return "id";

        return "sub";
    }

}