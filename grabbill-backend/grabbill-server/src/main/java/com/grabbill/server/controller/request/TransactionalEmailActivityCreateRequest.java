package com.grabbill.server.controller.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * @author michaellow
 */
@Data
public class TransactionalEmailActivityCreateRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    @Size(max = 255)
    private String emailFrom;

    @Size(max = 255)
    private String emailFromName;

    @Size(max = 255)
    private String emailSubject;

    private String emailContent;
    private String emailMjmlContent;

}
