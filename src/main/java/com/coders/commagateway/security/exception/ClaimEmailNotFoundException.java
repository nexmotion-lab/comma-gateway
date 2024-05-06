package com.coders.commagateway.security.exception;

public class ClaimEmailNotFoundException extends ClaimNotFoundException{
    public ClaimEmailNotFoundException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public ClaimEmailNotFoundException(String msg) {
        super(msg);
    }
}
