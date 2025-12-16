package com.grabbill.server.controller.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author michaellow
 **/
@Data
public class UserAccountUpdateSftpSettingsRequest {

    @NotNull
    private String host;

    @NotNull
    private Integer port;

    @NotNull
    private String username;

    @NotNull
    private String password;

}
