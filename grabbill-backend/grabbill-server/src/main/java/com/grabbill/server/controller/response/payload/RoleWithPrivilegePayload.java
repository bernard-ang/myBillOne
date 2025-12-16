package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.Privilege;
import com.grabbill.core.entity.Role;
import com.grabbill.server.controller.response.ApiPayload;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author seez
 */
@Data
@AllArgsConstructor
public class RoleWithPrivilegePayload implements ApiPayload {

    private List<RolePayload> roles;
    private List<RolePayload.PrivilegePayload> privileges;


    public static RoleWithPrivilegePayload from(
            final List<RolePayload> roles,
            List<Privilege> privileges
    ) {
        List<RolePayload.PrivilegePayload> rolePrivileges = new ArrayList<>();
        for (Privilege privilege : privileges) {
            RolePayload.PrivilegePayload payload = new RolePayload.PrivilegePayload();
            payload.setId(privilege.getId());
            payload.setName(privilege.getName());
            rolePrivileges.add(payload);
        }

        return new RoleWithPrivilegePayload(
                roles,
                rolePrivileges
        );
    }
}
