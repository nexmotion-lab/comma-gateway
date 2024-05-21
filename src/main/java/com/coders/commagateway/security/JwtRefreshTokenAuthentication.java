package com.coders.commagateway.security;

import com.auth0.jwt.interfaces.DecodedJWT;

public class JwtRefreshTokenAuthentication extends JwtTokenAuthentication{

    public JwtRefreshTokenAuthentication(DecodedJWT token) {
        super(token);
    }
}
