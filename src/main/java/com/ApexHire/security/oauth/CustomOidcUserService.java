package com.ApexHire.security.oauth;

import com.ApexHire.user.model.AuthProvider;
import com.ApexHire.user.model.Role;
import com.ApexHire.user.model.User;
import com.ApexHire.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

    private final UserRepository userRepository;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {

        // Get user information from Google (OIDC)
        OidcUser oidcUser = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId();

        String email = oidcUser.getEmail();

        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException("OAuth provider did not provide an email");
        }

        String finalEmail = email.toLowerCase().trim();

        String name = oidcUser.getFullName();
        if (name == null || name.isBlank()) {
            name = oidcUser.getGivenName() + " " + oidcUser.getFamilyName();
        }
        String finalName = (name == null || name.isBlank()) ? finalEmail : name;

        // Find existing user
        User user = userRepository
                .findByEmail(finalEmail)
                .orElseGet(() -> createOAuthUser(finalName, finalEmail, provider));

        /*
         * Existing EMAIL account:
         *
         * Do NOT automatically merge it with Google.
         */
        if (user.getProvider() == AuthProvider.EMAIL) {
            throw new OAuth2AuthenticationException("An account already exists with this email. " + "Please login using email/password.");
        }

        // Existing OAuth account
        if (!user.getProvider().name().equalsIgnoreCase(provider)) {
            throw new OAuth2AuthenticationException("This email is already linked to another OAuth provider.");
        }

        return new DefaultOidcUser(
                Set.of(new SimpleGrantedAuthority("ROLE_USER")),
                oidcUser.getIdToken(),
                oidcUser.getUserInfo(),
                "name"
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
}
