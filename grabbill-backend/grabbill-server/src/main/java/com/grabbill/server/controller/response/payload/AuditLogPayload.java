package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.AuditLog;
import com.grabbill.core.model.DomainType;
import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * @author michaellow
 */
@Data
public class AuditLogPayload implements ApiPayload {

    private Long id;

    private DomainType domainType;

    private String actionType;

    private Long parentTypeId;

    private Long targetId;

    private String description;

    private String createdBy;

    private OffsetDateTime createdDate;



    public static AuditLogPayload from (
            final AuditLog auditLog
    ) {
        AuditLogPayload instance = new AuditLogPayload();
        instance.setId(auditLog.getId());
        instance.setDomainType(auditLog.getDomainType());
        instance.setActionType(auditLog.getActionType());
        instance.setParentTypeId(auditLog.getParentTypeid());
        instance.setTargetId(auditLog.getTargetId());
        instance.setDescription(auditLog.getDescription());
        instance.setCreatedBy(auditLog.getCreatedBy());
        instance.setCreatedDate(auditLog.getCreatedDate());

        return instance;
    }

}
