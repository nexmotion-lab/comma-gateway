package com.coders.commagateway.security.exception;

public class ClaimSocialTypeNotFoundException extends ClaimNotFoundException{
    public ClaimSocialTypeNotFoundException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public ClaimSocialTypeNotFoundException(String msg) {
        super(msg);
    }
}
