package com.grabbill.server.exception;

import com.grabbill.server.GrabbillServerErrorCode;
import lombok.Getter;

/**
 * @author michaellow
 */
public class GrabbillServerException extends RuntimeException {

    @Getter
    private GrabbillServerErrorCode errorCode;

    public GrabbillServerException(
            final GrabbillServerErrorCode errorCode,
            final String message
    ) {
        super(message);
        this.errorCode = errorCode;
    }

    public GrabbillServerException(
            final GrabbillServerErrorCode errorCode,
            final String message,
            final Throwable throwable
    ) {
        super(message, throwable);
        this.errorCode = errorCode;
    }

}
