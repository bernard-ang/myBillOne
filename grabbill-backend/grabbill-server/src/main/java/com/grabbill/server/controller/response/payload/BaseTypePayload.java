package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.BaseType;
import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * @author michaellow
 */
@Data
public class BaseTypePayload implements ApiPayload {

    Long id;
    String name;
    String code;
    int noOfFiles;
    List<BaseIndexFieldPayload> indexFields;
    String createdBy;
    OffsetDateTime createdDate;
    String lastModifiedBy;
    OffsetDateTime lastModifiedDate;


    protected void copyFrom(final BaseType type) {
        this.setName(type.getName());
        this.setCode(type.getCode());
        this.setCreatedBy(type.getCreatedBy());
        this.setCreatedDate(type.getCreatedDate());
        this.setLastModifiedBy(type.getLastModifiedBy());
        this.setLastModifiedDate(type.getLastModifiedDate());
    }

}
