package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.BaseIndexField;
import com.grabbill.core.model.DataType;
import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

/**
 * @author michaellow
 */
@Data
public class BaseIndexFieldPayload implements ApiPayload {

    private Long id;

    private int seqOrder;

    private String label;

    private String header;

    private boolean required;

    private DataType dataType;

    private boolean referenced;

    private boolean applicable;


    public void copyFrom(final BaseIndexField targetIndexField) {
        this.setSeqOrder(targetIndexField.getSeqOrder());
        this.setLabel(targetIndexField.getLabel());
        this.setHeader(targetIndexField.getHeader());
        this.setRequired(targetIndexField.isRequired());
        this.setDataType(targetIndexField.getDataType());
        this.setApplicable(targetIndexField.isApplicable());
        this.setReferenced(targetIndexField.isHardRef() || targetIndexField.isSoftRef());
    }

}
