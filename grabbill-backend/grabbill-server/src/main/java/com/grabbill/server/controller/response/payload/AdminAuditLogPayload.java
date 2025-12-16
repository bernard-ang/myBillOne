package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.AdminAuditLog;
import com.grabbill.core.model.AdminDomainType;
import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * @author michaellow
 */
@Data
public class AdminAuditLogPayload implements ApiPayload {

    private Long id;

    private AdminDomainType adminDomainType;

    private Integer targetId;

    private String actionType;

    private String description;

    private String createdBy;

    private OffsetDateTime createdDate;



    public static AdminAuditLogPayload from (
            final AdminAuditLog adminAuditLog
    ) {
        AdminAuditLogPayload instance = new AdminAuditLogPayload();
        instance.setId(adminAuditLog.getId());
        instance.setAdminDomainType(adminAuditLog.getAdminDomainType());
        instance.setTargetId(adminAuditLog.getTargetId());
        instance.setActionType(adminAuditLog.getActionType());
        instance.setDescription(adminAuditLog.getDescription());
        instance.setCreatedBy(adminAuditLog.getCreatedBy());
        instance.setCreatedDate(adminAuditLog.getCreatedDate());

        return instance;
    }

}
