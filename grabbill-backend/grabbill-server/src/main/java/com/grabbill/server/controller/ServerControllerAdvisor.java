package com.grabbill.server.controller;

import com.grabbill.server.GrabbillServerErrorCode;
import com.grabbill.server.controller.response.ApiErrorMessage;
import com.grabbill.server.controller.response.GrabbillApiResponse;
import com.grabbill.server.controller.response.GrabbillServerApiVersion;
import com.grabbill.server.exception.GrabbillServerException;
import com.grabbill.server.exception.InactiveAccountException;
import com.grabbill.server.exception.UnverifiedAccountException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * @author michaellow
 */
@ControllerAdvice(basePackages = {
        "com.grabbill.server.controller",
        "com.grabbill.server.security"
})
public class ServerControllerAdvisor extends ResponseEntityExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerControllerAdvisor.class);


    @ExceptionHandler(Exception.class)
    @ResponseBody
    ResponseEntity<GrabbillApiResponse> handleUnexpectedException(final Exception ex) {
        GrabbillServerErrorCode errorCode = GrabbillServerErrorCode.GRB0001;
        LOGGER.error(errorCode.getErrorMessage(), ex);
        return new ResponseEntity<>(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        new ApiErrorMessage(
                                errorCode.name(),
                                errorCode.getErrorMessage()
                        )
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    protected ResponseEntity<Object> handleAllAuthenticationException(AuthenticationException ex) {
        GrabbillServerErrorCode errorCode = GrabbillServerErrorCode.GRB0010;
        LOGGER.error(errorCode.getErrorMessage(), ex);
        return new ResponseEntity<>(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        new ApiErrorMessage(
                                errorCode.name(),
                                errorCode.getErrorMessage()
                        )
                ),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(UnverifiedAccountException.class)
    protected ResponseEntity<Object> handleAllAuthenticationException(UnverifiedAccountException ex) {
        GrabbillServerErrorCode errorCode = GrabbillServerErrorCode.GRB0013;
        LOGGER.error(errorCode.getErrorMessage(), ex);
        return new ResponseEntity<>(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        new ApiErrorMessage(
                                errorCode.name(),
                                errorCode.getErrorMessage()
                        )
                ),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(InactiveAccountException.class)
    protected ResponseEntity<Object> handleAllAuthenticationException(InactiveAccountException ex) {
        GrabbillServerErrorCode errorCode = GrabbillServerErrorCode.GRB0014;
        LOGGER.error(errorCode.getErrorMessage(), ex);
        return new ResponseEntity<>(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        new ApiErrorMessage(
                                errorCode.name(),
                                errorCode.getErrorMessage()
                        )
                ),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(GrabbillServerException.class)
    protected ResponseEntity<Object> handleAllBusinessException(GrabbillServerException ex) {
        GrabbillServerErrorCode errorCode = ex.getErrorCode() != null ? ex.getErrorCode() : GrabbillServerErrorCode.GRB0001;
        String errorMessage = errorCode.getErrorMessage() + " - " + ex.getMessage();
        LOGGER.warn(errorMessage, ex);

        if (GrabbillServerErrorCode.GRB8005.equals(errorCode)) {
            return new ResponseEntity<>(
                    new GrabbillApiResponse(
                            GrabbillServerApiVersion.V1.getVersion(),
                            new ApiErrorMessage(
                                    errorCode.name(),
                                    errorMessage
                            )
                    ),
                    HttpStatus.PAYMENT_REQUIRED
            );

        } else {
            return new ResponseEntity<>(
                    new GrabbillApiResponse(
                            GrabbillServerApiVersion.V1.getVersion(),
                            new ApiErrorMessage(
                                    errorCode.name(),
                                    errorMessage
                            )
                    ),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            final MethodArgumentNotValidException ex,
            final HttpHeaders headers,
            final HttpStatus status,
            final WebRequest request
    ) {
        GrabbillServerErrorCode errorCode = GrabbillServerErrorCode.GRB0002;
        LOGGER.error(errorCode.getErrorMessage(), ex);
        return new ResponseEntity<>(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        new ApiErrorMessage(
                                errorCode.name(),
                                errorCode.getErrorMessage()
                        )
                ),
                HttpStatus.BAD_REQUEST
        );
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            final MissingServletRequestParameterException ex,
            final HttpHeaders headers,
            final HttpStatus status,
            final WebRequest request
    ) {
        GrabbillServerErrorCode errorCode = GrabbillServerErrorCode.GRB0003;
        LOGGER.error(errorCode.getErrorMessage(), ex);
        return new ResponseEntity<>(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        new ApiErrorMessage(
                                errorCode.name(),
                                errorCode.getErrorMessage()
                        )
                ),
                HttpStatus.BAD_REQUEST
        );
    }

    @Override
    protected ResponseEntity<Object> handleServletRequestBindingException(
            final ServletRequestBindingException ex,
            final HttpHeaders headers,
            final HttpStatus status,
            final WebRequest request
    ) {
        GrabbillServerErrorCode errorCode = GrabbillServerErrorCode.GRB0003;
        LOGGER.error(errorCode.getErrorMessage(), ex);
        return new ResponseEntity<>(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        new ApiErrorMessage(
                                errorCode.name(),
                                errorCode.getErrorMessage()
                        )
                ),
                HttpStatus.BAD_REQUEST
        );
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            final HttpMediaTypeNotSupportedException ex,
            final HttpHeaders headers,
            final HttpStatus status,
            final WebRequest request
    ) {
        GrabbillServerErrorCode errorCode = GrabbillServerErrorCode.GRB0004;
        LOGGER.error(errorCode.getErrorMessage(), ex);
        return new ResponseEntity<>(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        new ApiErrorMessage(
                                errorCode.name(),
                                errorCode.getErrorMessage()
                        )
                ),
                HttpStatus.BAD_REQUEST
        );
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(
            final HttpMediaTypeNotAcceptableException ex,
            final HttpHeaders headers,
            final HttpStatus status,
            final WebRequest request
    ) {
        GrabbillServerErrorCode errorCode = GrabbillServerErrorCode.GRB0005;
        LOGGER.error(errorCode.getErrorMessage(), ex);
        return new ResponseEntity<>(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        new ApiErrorMessage(
                                errorCode.name(),
                                errorCode.getErrorMessage()
                        )
                ),
                HttpStatus.NOT_ACCEPTABLE
        );
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            final HttpRequestMethodNotSupportedException ex,
            final HttpHeaders headers,
            final HttpStatus status,
            final WebRequest request
    ) {
        GrabbillServerErrorCode errorCode = GrabbillServerErrorCode.GRB0006;
        LOGGER.error(errorCode.getErrorMessage(), ex);
        return new ResponseEntity<>(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        new ApiErrorMessage(
                                errorCode.name(),
                                errorCode.getErrorMessage()
                        )
                ),
                HttpStatus.METHOD_NOT_ALLOWED
        );
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            final HttpMessageNotReadableException ex,
            final HttpHeaders headers,
            final HttpStatus status,
            final WebRequest request
    ) {
        GrabbillServerErrorCode errorCode = GrabbillServerErrorCode.GRB0011;
        LOGGER.error(errorCode.getErrorMessage(), ex);
        return new ResponseEntity<>(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        new ApiErrorMessage(
                                errorCode.name(),
                                errorCode.getErrorMessage()
                        )
                ),
                HttpStatus.BAD_REQUEST
        );
    }

}
