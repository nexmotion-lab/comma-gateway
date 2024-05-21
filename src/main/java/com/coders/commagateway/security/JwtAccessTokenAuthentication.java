package com.coders.commagateway.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.coders.commagateway.security.JwtTokenAuthentication;
import com.coders.commagateway.security.jwt.JwtService;

public class JwtAccessTokenAuthentication extends JwtTokenAuthentication {

    public JwtAccessTokenAuthentication(DecodedJWT token) {
        super(token);
        super.setRole(token.getClaim(JwtService.ROLE).asString());
    }
}
