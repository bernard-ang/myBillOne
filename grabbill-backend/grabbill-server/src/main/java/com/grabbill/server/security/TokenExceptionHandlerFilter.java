package com.grabbill.server.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grabbill.server.GrabbillServerErrorCode;
import com.grabbill.server.controller.response.ApiErrorMessage;
import com.grabbill.server.controller.response.GrabbillApiResponse;
import com.grabbill.server.controller.response.GrabbillServerApiVersion;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * To handle {@link ExpiredJwtException} thrown from request filter which does not handle by controller advisor.
 *
 * @author michaellow
 */
public class TokenExceptionHandlerFilter extends OncePerRequestFilter {

    private static final ObjectMapper mapper = new ObjectMapper();


    @Override
    protected void doFilterInternal(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            GrabbillServerErrorCode errorCode = GrabbillServerErrorCode.GRB0009;
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write(convertObjectToJson(
                    new GrabbillApiResponse(
                            GrabbillServerApiVersion.V1.getVersion(),
                            new ApiErrorMessage(
                                    errorCode.name(),
                                    errorCode.getErrorMessage()
                            )
                    )
            ));
        }
    }

    private String convertObjectToJson(final Object object) throws JsonProcessingException {
        if (object == null) {
            return null;
        }
        return mapper.writeValueAsString(object);
    }

}
