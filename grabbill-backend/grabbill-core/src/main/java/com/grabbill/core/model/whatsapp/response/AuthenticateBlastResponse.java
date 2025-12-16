package com.grabbill.core.model.whatsapp.response;

import lombok.Data;

import java.util.Date;

@Data
public class AuthenticateBlastResponse {
    private String email;
    private String name;
    private String userId;
    private boolean isverify;
    private String token;
    private Date expiryDate;
}
