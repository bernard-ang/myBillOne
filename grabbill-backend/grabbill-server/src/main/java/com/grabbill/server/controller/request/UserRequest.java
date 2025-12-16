package com.grabbill.server.controller.request;

import com.grabbill.core.entity.Role;
import com.grabbill.core.entity.User;
import com.grabbill.core.entity.UserCode;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

/**
 * @author seez
 */
@Data
public class UserRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String email;

    @NotBlank
    private String role;

    private List<String> codes = new ArrayList<>();


    public void to(final User user, final Role role) {
        user.setName(this.name);
        user.setEmail(this.email);
        user.setRole(role);

        user.getUserCodes().clear();
        for (String code : codes) {
            UserCode userCode = new UserCode();
            userCode.setCode(code);
            userCode.setUser(user);

            user.getUserCodes().add(userCode);
        }
    }
}
