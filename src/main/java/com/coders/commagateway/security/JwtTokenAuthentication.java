package com.coders.commagateway.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.coders.commagateway.security.jwt.JwtService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;

public class JwtTokenAuthentication implements Authentication {

    private String token;
    private boolean isVerified = false;
    private CustomPrincipal customPrincipal;
    private String role;
    private final JwtService jwtService = new JwtService();

    public JwtTokenAuthentication(DecodedJWT token) {
        this.token = token.getToken();
        this.customPrincipal = new CustomPrincipal(token.getClaim(jwtService.EMAIL_CLAIM).asString(),
                token.getClaim(jwtService.SOCIAL_TYPE_CLAIM).asString());
        this.role = token.getClaim(jwtService.ROLE).asString();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (role == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return customPrincipal;
    }

    @Override
    public boolean isAuthenticated() {
        return isVerified;
    }

    @Override
    public void setAuthenticated(boolean isVerified) throws IllegalArgumentException {
        this.isVerified = isVerified;
    }

    @Override
    public String getName() {
        if (customPrincipal != null) {
            return customPrincipal.getName();
        }
        return null;
    }

}
