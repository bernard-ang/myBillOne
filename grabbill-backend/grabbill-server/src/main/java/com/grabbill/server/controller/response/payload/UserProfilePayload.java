package com.grabbill.server.controller.response.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.grabbill.core.entity.User;
import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

/**
 * @author michaellow
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProfilePayload implements ApiPayload {

    private String name;

    private boolean google2FAEnabled;

    private boolean email2FAEnabled;


    public static UserProfilePayload from(final User user) {
        UserProfilePayload instance = new UserProfilePayload();
        instance.name = user.getName();
        instance.google2FAEnabled = user.isGoogle2FAEnabled();
        instance.email2FAEnabled = user.isEmail2FAEnabled();

        return instance;
    }

}
