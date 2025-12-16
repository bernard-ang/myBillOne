package com.grabbill.engine.job;

import com.grabbill.core.exception.GrabbillException;

/**
 * @author michaellow
 */
public class JobProcessingException extends GrabbillException {

    public JobProcessingException(final String s) {
        super(s);
    }

    public JobProcessingException(final String s, final Throwable throwable) {
        super(s, throwable);
    }

}
