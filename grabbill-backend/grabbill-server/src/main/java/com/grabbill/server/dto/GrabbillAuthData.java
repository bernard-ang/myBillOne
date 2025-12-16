package com.grabbill.server.dto;

import com.grabbill.server.controller.response.GrabbillApiResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpHeaders;

/**
 * @author michaellow
 */
@AllArgsConstructor
@Data
public class GrabbillAuthData {

    private HttpHeaders headers;

    private GrabbillApiResponse body;

}

