package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.User;
import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author michaellow
 */
@Data
public class AccountUsersPayload implements ApiPayload {

    private List<UserPayload> users = new ArrayList<>();


    public static AccountUsersPayload from(final List<User> users) {
        AccountUsersPayload payload = new AccountUsersPayload();

        for (User user : users) {
            UserPayload userPayload = UserPayload.from(user);
            payload.getUsers().add(userPayload);
        }

        return payload;
    }

}
