package com.grabbill.server.controller.request;

import com.grabbill.core.entity.SmsIndexField;
import com.grabbill.core.entity.SmsType;
import com.grabbill.core.model.SmsFieldType;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author michaellow
 */
@Data
public class SmsTypeRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    @Size(max = 255)
    private String code;

    @Size(max = 255)
    private String smsFrom;

    @Size(max = 255)
    private String smsContent;

    @NotNull
    private SmsFieldType smsFieldType;

    private List<BaseIndexFieldRequest> indexFields = new ArrayList<>();

    private Integer contactGroupId;


    public void to(final SmsType smsType) {
        smsType.setName(this.name);
        smsType.setCode(this.code);
        smsType.setSmsFrom(this.smsFrom);
        smsType.setSmsContent(this.smsContent);
        smsType.setSmsFieldType(this.smsFieldType);

        // sort request and target indexFields according to seqOrder field
        List<SmsIndexField> targetSmsIndexFields = smsType.getSmsIndexFields();
        targetSmsIndexFields.sort(Comparator.comparingInt(SmsIndexField::getSeqOrder));
        indexFields.sort(Comparator.comparingInt(BaseIndexFieldRequest::getSeqOrder));

        int i = 0;
        for (; i < indexFields.size(); i++) {
            if (targetSmsIndexFields.size() > i) {
                indexFields.get(i).to(targetSmsIndexFields.get(i));

            } else {
                SmsIndexField indexField = new SmsIndexField();
                indexField.setSmsType(smsType);
                indexFields.get(i).to(indexField);
                smsType.getSmsIndexFields().add(indexField);
            }
        }

        int currentSize = targetSmsIndexFields.size();
        int newSize = indexFields.size();
        for (; i < currentSize; i++) {
            smsType.getSmsIndexFields().remove(newSize);
        }
    }

}
