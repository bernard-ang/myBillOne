package com.grabbill.server.exception;

/**
 * @author michaellow
 */
public class UnverifiedAccountException extends RuntimeException {

    public UnverifiedAccountException(final String message) {
        super(message);
    }

    public UnverifiedAccountException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
