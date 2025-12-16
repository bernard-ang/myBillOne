package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.Privilege;
import com.grabbill.core.entity.Role;
import com.grabbill.server.controller.response.ApiPayload;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author michaellow
 */
@Data
@AllArgsConstructor
public class RolePayload implements ApiPayload {

    private Integer id;
    private String name;
    private List<PrivilegePayload> privileges;


    @Data
    public static class PrivilegePayload {
        private Integer id;
        private String name;
    }

    public static RolePayload from(
            final Role role
    ) {
        List<PrivilegePayload> privileges = new ArrayList<>();
        for (Privilege privilege : role.getPrivileges()) {
            PrivilegePayload payload = new PrivilegePayload();
            payload.setId(privilege.getId());
            payload.setName(privilege.getName());
            privileges.add(payload);
        }

        return new RolePayload(
                role.getId(),
                role.getName(),
                privileges
        );
    }

}
