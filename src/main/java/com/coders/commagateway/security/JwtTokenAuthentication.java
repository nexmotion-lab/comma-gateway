package com.coders.commagateway.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.coders.commagateway.security.jwt.JwtService;
import com.coders.commagateway.security.jwt.TokenResponse;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class JwtTokenAuthentication implements Authentication {

    private String token;
    private boolean isVerified = false;
    private String email;
    @Setter
    private String role;


    @Getter
    @Setter
    private TokenResponse reissuanceToken;


    public JwtTokenAuthentication(DecodedJWT token) {
        this.token = token.getToken();
        this.email = token.getClaim(JwtService.EMAIL_CLAIM).asString();
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
        return email;
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
        return email;
    }

    public void setRole(String role) {
        this.role = role;
    }

}
