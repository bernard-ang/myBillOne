package com.grabbill.core.exception;

/**
 * @author michaellow
 */
public class GrabbillSftpException extends RuntimeException {

    public GrabbillSftpException(final String message) {
        super(message);
    }

    public GrabbillSftpException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
