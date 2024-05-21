package com.coders.commagateway.security.exception;

import org.springframework.security.core.AuthenticationException;

public class CreateTokenException extends AuthenticationException {
    public CreateTokenException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public CreateTokenException(String msg) {
        super(msg);
    }
}
