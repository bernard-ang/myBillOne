package com.grabbill.server.dto;

import lombok.Data;

/**
 * @author michaellow
 */
@Data
public class FcmIdTokenPayload {

    private String uid;

    private String email;

    private String name;

}
