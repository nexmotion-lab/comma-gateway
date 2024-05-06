package com.coders.commagateway.security.exception;

import org.springframework.security.core.AuthenticationException;

public class ClaimNotFoundException extends AuthenticationException {

    public ClaimNotFoundException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public ClaimNotFoundException(String msg) {
        super(msg);
    }
}
