package com.grabbill.engine.service;

/**
 * @author michaellow
 */
public class DsnReaderException extends RuntimeException {

    public DsnReaderException(final String message) {
        super(message);
    }

    public DsnReaderException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
