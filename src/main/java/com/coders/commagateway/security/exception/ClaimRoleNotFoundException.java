package com.coders.commagateway.security.exception;

public class ClaimRoleNotFoundException extends ClaimNotFoundException{
    public ClaimRoleNotFoundException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public ClaimRoleNotFoundException(String msg) {
        super(msg);
    }
}
