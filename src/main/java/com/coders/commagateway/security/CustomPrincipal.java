package com.coders.commagateway.security;

import lombok.Getter;

@Getter
public class CustomPrincipal implements java.security.Principal {

    private String email;
    private String socialType;

    public CustomPrincipal(String email, String socialType) {
        this.email = email;
        this.socialType = socialType;
    }

    @Override
    public String getName() {
        return email + ":" + socialType;
    }
}
