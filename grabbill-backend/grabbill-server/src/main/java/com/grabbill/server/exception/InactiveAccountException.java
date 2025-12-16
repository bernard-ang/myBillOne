package com.grabbill.server.exception;

/**
 * @author michaellow
 */
public class InactiveAccountException extends RuntimeException {

    public InactiveAccountException(final String message) {
        super(message);
    }

    public InactiveAccountException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
