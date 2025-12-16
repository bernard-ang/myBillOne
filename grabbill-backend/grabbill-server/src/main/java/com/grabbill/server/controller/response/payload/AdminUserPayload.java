package com.grabbill.server.controller.response.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.grabbill.core.entity.AdminUser;
import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * @author michaellow
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminUserPayload implements ApiPayload {

    private Integer id;

    private String name;

    private String email;

    private boolean active;

    private boolean google2FAEnabled;

    private boolean email2FAEnabled;

    private OffsetDateTime lastLoggedIn;

    private String createdBy;

    private OffsetDateTime createdDate;


    public static AdminUserPayload from(final AdminUser adminUser) {
        AdminUserPayload instance = new AdminUserPayload();
        instance.id = adminUser.getId();
        instance.name = adminUser.getName();
        instance.email = adminUser.getEmail();
        instance.active = adminUser.isActive();
        instance.google2FAEnabled = adminUser.isGoogle2FAEnabled();
        instance.email2FAEnabled = adminUser.isEmail2FAEnabled();
        instance.lastLoggedIn = adminUser.getLastLoggedIn();
        instance.createdBy = adminUser.getCreatedBy();
        instance.createdDate = adminUser.getCreatedDate();

        return instance;
    }

}
