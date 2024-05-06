package com.coders.commagateway.security.exception;


import org.springframework.security.core.AuthenticationException;

public class TokenMissingException extends AuthenticationException {

    public TokenMissingException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public TokenMissingException(String msg) {
        super(msg);
    }
}
