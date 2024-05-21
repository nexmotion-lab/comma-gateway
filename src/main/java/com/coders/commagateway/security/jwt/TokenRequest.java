package com.coders.commagateway.security.jwt;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenRequest {

    private String refreshToken;
    private String accessToken;

    public TokenRequest() {
    }
}
