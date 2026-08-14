package com.ApexHire.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OAuthTokenResponse {

    private String accessToken;
    private String refreshToken;

}