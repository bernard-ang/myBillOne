package com.grabbill.server.controller;

import com.grabbill.core.entity.Privilege;
import com.grabbill.core.entity.Role;
import com.grabbill.core.entity.User;
import com.grabbill.core.service.PrivilegeService;
import com.grabbill.core.service.RoleService;
import com.grabbill.core.service.UserService;
import com.grabbill.server.GrabbillServerErrorCode;
import com.grabbill.server.controller.request.RoleRequest;
import com.grabbill.server.controller.response.ApiMessage;
import com.grabbill.server.controller.response.GrabbillApiResponse;
import com.grabbill.server.controller.response.GrabbillServerApiVersion;
import com.grabbill.server.controller.response.payload.RolePayload;
import com.grabbill.server.controller.response.payload.RoleWithPrivilegePayload;
import com.grabbill.server.controller.response.payload.RolesPayload;
import com.grabbill.server.dto.GrabbillUserDetails;
import com.grabbill.server.exception.GrabbillServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides endpoint to retrieve roles.
 *
 * @author seez
 */
@Transactional
@RestController
@RequestMapping("/roles")
public class RoleController {

    @Autowired
    private PrivilegeService privilegeService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserService userService;


    @GetMapping()
    public ResponseEntity<GrabbillApiResponse> getRoles() {
        List<Role> roles = roleService.getAll();
        List<String> roleList = roles.stream().map(Role::getName).collect(Collectors.toList());

        GrabbillApiResponse response = new GrabbillApiResponse(
                GrabbillServerApiVersion.V1.getVersion(),
                new RolesPayload(roleList)
        );
        return ResponseEntity.ok().body(response);
    }

    @GetMapping(params = {"privilege=1"})
    public ResponseEntity<GrabbillApiResponse> getRolesWithPrivileges() {
        List<Role> roles = roleService.getAll();
        List<Privilege> privileges = privilegeService.getAll();

        GrabbillApiResponse response = new GrabbillApiResponse(
                GrabbillServerApiVersion.V1.getVersion(),
                RoleWithPrivilegePayload.from(
                        roles.stream().map(RolePayload::from).collect(Collectors.toList()),
                        privileges
                )
        );
        return ResponseEntity.ok().body(response);
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<GrabbillApiResponse> getRoleDetails(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Integer id
    ) {
        validateUser(getUser(userDetails));
        Role role = roleService.getById(id).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB1005,
                        "Role [" + id + "] is not found!"
                )
        );

        GrabbillApiResponse response = new GrabbillApiResponse(
                GrabbillServerApiVersion.V1.getVersion(),
                RolePayload.from(role)
        );
        return ResponseEntity.ok().body(response);
    }

    @PostMapping()
    public ResponseEntity<GrabbillApiResponse> createNewRole(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @Valid @RequestBody RoleRequest request
    ) {
        validateUser(getUser(userDetails));
        if (roleService.getByName(request.getName()).isPresent()) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB1042,
                    "Role [" + request.getName() + "] is already exist!"
            );
        }

        Role role = new Role();
        role.setName(request.getName());
        List<Privilege> privileges = privilegeService.getByIds(request.getPrivilegeIds());
        role.setPrivileges(privileges);
        role = roleService.save(role);

        GrabbillApiResponse response = new GrabbillApiResponse(
                GrabbillServerApiVersion.V1.getVersion(),
                RolePayload.from(role)
        );
        return ResponseEntity.ok().body(response);
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<GrabbillApiResponse> updateRoleDetails(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Integer id,
            @Valid @RequestBody RoleRequest request
    ) {
        validateUser(getUser(userDetails));
        Role role = roleService.getById(id).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB1005,
                        "Role [" + id + "] is not found!"
                )
        );

        if (role.isOwner()) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB1044,
                    "Role [" + role.getName() + "] is not editable!"
            );
        }

        List<Privilege> privileges = privilegeService.getByIds(request.getPrivilegeIds());
        role.setPrivileges(privileges);
        role.setName(request.getName());
        role = roleService.save(role);

        GrabbillApiResponse response = new GrabbillApiResponse(
                GrabbillServerApiVersion.V1.getVersion(),
                RolePayload.from(role)
        );
        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<GrabbillApiResponse> deleteRole(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Integer id
    ) {
        validateUser(getUser(userDetails));
        Role role = roleService.getById(id).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB1005,
                        "Role [" + id + "] is not found!"
                )
        );

        if (role.isOwner()) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB1044,
                    "Role [" + role.getName() + "] is not deletable!"
            );
        }

        if (!role.getUsers().isEmpty()) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB1045,
                    "Role [" + role.getName() + "] is not deletable!"
            );
        }

        roleService.delete(role);

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        new ApiMessage("Role with id [" + id + "] removed successfully.")
                )
        );
    }

    private User getUser(final GrabbillUserDetails userDetails) {
        return userService.getByEmail(userDetails.getUsername()).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB1002,
                        "User [" + userDetails.getUsername() + "] is not found!"
                )
        );
    }

    private void validateUser(final User user) {
        if (!user.getRole().isOwner()) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB1043,
                    "User [" + user.getName() + "] is not owner!"
            );
        }
    }

}
