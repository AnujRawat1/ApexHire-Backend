package com.ApexHire.security.constants;

public class PublicAPIs {
    public static final String[] PUBLIC_APIS = {
            "/api/health",

            // Swagger
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",

            // Auth
            "/api/auth/sign-up",
            "/api/auth/verify-email",
            "/api/auth/resend-verification",
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/auth/logout",
            "/api/auth/oauth/exchange",

            // OAuth2
            "/oauth2/**",
            "/login/oauth2/**"
    };
}
