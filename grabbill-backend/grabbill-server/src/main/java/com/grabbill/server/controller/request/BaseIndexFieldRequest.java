package com.grabbill.server.controller.request;

import com.grabbill.core.entity.BaseIndexField;
import com.grabbill.core.model.DataType;
import lombok.Data;

import javax.validation.constraints.Size;

/**
 * @author michaellow
 */
@Data
public class BaseIndexFieldRequest {

    private int seqOrder;

    @Size(max = 255)
    private String label;

    @Size(max = 255)
    private String header;

    private boolean required;

    private DataType dataType;

    private boolean applicable;


    public void to(final BaseIndexField indexField) {
        indexField.setSeqOrder(this.seqOrder);
        indexField.setLabel(this.label);
        indexField.setHeader(this.header);
        indexField.setRequired(this.required);
        indexField.setDataType(this.dataType);
        indexField.setApplicable(this.applicable);
    }

}
