package com.grabbill.core.exception;

/**
 * @author michaellow
 */
public class GrabbillException extends RuntimeException {

    public GrabbillException(final String message) {
        super(message);
    }

    public GrabbillException(final Throwable throwable) {
        super(throwable);
    }

    public GrabbillException(final String message, final Throwable throwable) {
        super(message, throwable);
    }

}
