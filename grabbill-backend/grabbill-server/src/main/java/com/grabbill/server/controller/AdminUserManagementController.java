package com.grabbill.server.controller;

import com.grabbill.core.entity.AdminAuditLog;
import com.grabbill.core.entity.AdminUser;
import com.grabbill.core.model.AdminActionType;
import com.grabbill.core.model.AdminDomainType;
import com.grabbill.core.service.AdminAuditLogService;
import com.grabbill.core.service.AdminUserService;
import com.grabbill.server.GrabbillServerErrorCode;
import com.grabbill.server.controller.request.AdminUserRequest;
import com.grabbill.server.controller.response.ApiMessage;
import com.grabbill.server.controller.response.GrabbillApiResponse;
import com.grabbill.server.controller.response.GrabbillServerApiVersion;
import com.grabbill.server.controller.response.payload.AdminUserPayload;
import com.grabbill.server.controller.response.payload.SearchResultPayload;
import com.grabbill.server.dto.GrabbillAdminUserDetails;
import com.grabbill.server.exception.GrabbillServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

/**
 * @author michaellow
 */
@RestController
@RequestMapping("/mgmt/admin-users")
public class AdminUserManagementController extends BaseManagementController {

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private AdminAuditLogService adminAuditLogService;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Transactional
    @GetMapping()
    public ResponseEntity<GrabbillApiResponse> getAdminUsers(
            @AuthenticationPrincipal GrabbillAdminUserDetails userDetails,
            @RequestParam(required = false) String name,
            @PageableDefault(sort = {"createdDate"}, direction = Sort.Direction.DESC) Pageable pageable
    ) {
        checkStatus(userDetails);

        Page<AdminUser> page;
        if (StringUtils.hasLength(name)) {
            page = adminUserService.getAllByName(name, pageable);

        } else {
            page = adminUserService.getAll(pageable);
        }

        SearchResultPayload<AdminUserPayload> searchResultPayload =
                SearchResultPayload.<AdminUserPayload>builder()
                        .items(page.get()
                                .map(AdminUserPayload::from)
                                .collect(Collectors.toList())
                        )
                        .totalItems(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .build();

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        searchResultPayload
                )
        );
    }

    @Transactional
    @PostMapping()
    public ResponseEntity<GrabbillApiResponse> addNewAdminUser(
            @AuthenticationPrincipal GrabbillAdminUserDetails userDetails,
            @RequestBody AdminUserRequest request
    ) {
        checkStatus(userDetails);

        if (adminUserService.getByEmail(request.getEmail()).isPresent()) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB7003,
                    "Admin User with email [" + request.getEmail() + "] already exist!"
            );
        }

        AdminUser instance = new AdminUser();
        instance.setName(request.getName());
        instance.setEmail(request.getEmail());
        instance.setPassword(passwordEncoder.encode(request.getPassword()));
        instance.setActive(true);

        AdminUser updatedInstance = adminUserService.save(instance);

        AdminAuditLog adminAuditLog = new AdminAuditLog();
        adminAuditLog.setAdminDomainType(AdminDomainType.ADMIN_USER);
        adminAuditLog.setTargetId(updatedInstance.getId());
        adminAuditLog.setActionType(AdminActionType.ADMIN_USER_CREATE.name());
        adminAuditLog.setDescription("Admin account for [" + updatedInstance.getEmail() + "] created");
        adminAuditLogService.save(adminAuditLog);

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        AdminUserPayload.from(updatedInstance)
                )
        );
    }

    @Transactional
    @PutMapping(path = "/{adminUserId}")
    public ResponseEntity<GrabbillApiResponse> updateAdminUser(
            @AuthenticationPrincipal GrabbillAdminUserDetails userDetails,
            @PathVariable Integer adminUserId,
            @RequestBody AdminUserRequest request
    ) {
        checkStatus(userDetails);

        AdminUser target = adminUserService.getById(adminUserId).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB7001,
                        "Admin User with ID [" + adminUserId + "] is not found!"
                )
        );

        target.setName(request.getName());
        target.setEmail(request.getEmail());
        if(request.getPassword() != null) {
            target.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        AdminUser updatedTarget = adminUserService.save(target);

        AdminAuditLog adminAuditLog = new AdminAuditLog();
        adminAuditLog.setAdminDomainType(AdminDomainType.ADMIN_USER);
        adminAuditLog.setTargetId(updatedTarget.getId());
        adminAuditLog.setActionType(AdminActionType.ADMIN_USER_UPDATE.name());
        adminAuditLog.setDescription("Admin account for [" + updatedTarget.getEmail() + "] is updated");
        adminAuditLogService.save(adminAuditLog);

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        AdminUserPayload.from(updatedTarget)
                )
        );
    }

    @Transactional
    @PutMapping(path = "/{adminUserId}/activate")
    public ResponseEntity<GrabbillApiResponse> activateAdminUser(
            @AuthenticationPrincipal GrabbillAdminUserDetails userDetails,
            @PathVariable Integer adminUserId
    ) {
        checkStatus(userDetails);

        AdminUser target = adminUserService.getById(adminUserId).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB7001,
                        "Admin User with ID [" + adminUserId + "] is not found!"
                )
        );

        target.setActive(true);
        AdminUser updatedTarget = adminUserService.save(target);

        AdminAuditLog adminAuditLog = new AdminAuditLog();
        adminAuditLog.setAdminDomainType(AdminDomainType.ADMIN_USER);
        adminAuditLog.setTargetId(updatedTarget.getId());
        adminAuditLog.setActionType(AdminActionType.ADMIN_USER_ACTIVATE.name());
        adminAuditLog.setDescription("Admin account for [" + updatedTarget.getEmail() + "] is activate");
        adminAuditLogService.save(adminAuditLog);

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        AdminUserPayload.from(updatedTarget)
                )
        );
    }

    @Transactional
    @PutMapping(path = "/{adminUserId}/deactivate")
    public ResponseEntity<GrabbillApiResponse> deactivateAdminUser(
            @AuthenticationPrincipal GrabbillAdminUserDetails userDetails,
            @PathVariable Integer adminUserId
    ) {
        checkStatus(userDetails);

        AdminUser target = adminUserService.getById(adminUserId).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB7001,
                        "Admin User with ID [" + adminUserId + "] is not found!"
                )
        );

        target.setActive(false);
        AdminUser updatedTarget = adminUserService.save(target);

        AdminAuditLog adminAuditLog = new AdminAuditLog();
        adminAuditLog.setAdminDomainType(AdminDomainType.ADMIN_USER);
        adminAuditLog.setTargetId(updatedTarget.getId());
        adminAuditLog.setActionType(AdminActionType.ADMIN_USER_DEACTIVATE.name());
        adminAuditLog.setDescription("Admin account for [" + updatedTarget.getEmail() + "] is deactivate");
        adminAuditLogService.save(adminAuditLog);

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        AdminUserPayload.from(updatedTarget)
                )
        );
    }

    @Transactional
    @DeleteMapping(value = "/{adminUserId}")
    public ResponseEntity<GrabbillApiResponse> deleteAdminUser(
            @AuthenticationPrincipal GrabbillAdminUserDetails userDetails,
            @PathVariable Integer adminUserId
    ) {
        checkStatus(userDetails);

        AdminUser target = adminUserService.getById(adminUserId).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB7001,
                        "Admin User with ID [" + adminUserId + "] is not found!"
                )
        );

        adminUserService.delete(target);

        AdminAuditLog adminAuditLog = new AdminAuditLog();
        adminAuditLog.setAdminDomainType(AdminDomainType.ADMIN_USER);
        adminAuditLog.setTargetId(target.getId());
        adminAuditLog.setActionType(AdminActionType.ADMIN_USER_DELETE.name());
        adminAuditLog.setDescription("Admin account for [" + target.getEmail() + "] is deleted");
        adminAuditLogService.save(adminAuditLog);

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        new ApiMessage("Admin User with id [" + adminUserId + "] removed successfully.")
                )
        );
    }

}
