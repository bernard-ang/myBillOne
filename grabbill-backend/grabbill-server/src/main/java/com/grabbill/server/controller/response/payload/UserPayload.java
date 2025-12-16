package com.grabbill.server.controller.response.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.grabbill.core.entity.User;
import com.grabbill.core.entity.UserCode;
import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author seez
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserPayload implements ApiPayload {

    private Integer id;

    private String name;

    private String email;

    private boolean active;

    private boolean verified;

    private boolean accountActive;

    private boolean guidedStepsViewed;

    private boolean google2FAEnabled;

    private boolean email2FAEnabled;

    private String role;

    private OffsetDateTime lastLoggedIn;

    private List<String> codes = new ArrayList<>();


    public static UserPayload from(final User user) {
        UserPayload instance = new UserPayload();
        instance.id = user.getId();
        instance.name = user.getName();
        instance.email = user.getEmail();
        instance.role = user.getRole().getName();
        instance.active = user.isActive();
        instance.verified = user.isVerified();
        instance.accountActive = user.isAccountActive();
        instance.guidedStepsViewed = user.isGuidedStepsViewed();
        instance.google2FAEnabled = user.isGoogle2FAEnabled();
        instance.email2FAEnabled = user.isEmail2FAEnabled();
        instance.lastLoggedIn = user.getLastLoggedIn();

        for (UserCode userCode : user.getUserCodes()) {
            instance.codes.add(userCode.getCode());
        }

        return instance;
    }

}
