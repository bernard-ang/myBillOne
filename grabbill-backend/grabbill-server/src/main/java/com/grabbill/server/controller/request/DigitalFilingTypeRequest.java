package com.grabbill.server.controller.request;

import com.grabbill.core.entity.DigitalFilingIndexField;
import com.grabbill.core.entity.DigitalFilingType;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author michaellow
 */
@Data
public class DigitalFilingTypeRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    @Size(max = 255)
    private String code;

    private boolean autoPurge;

    private Integer autoPurgeByDays;

    private String csvSeparator;

    private List<BaseIndexFieldRequest> indexFields = new ArrayList<>();


    public void to(final DigitalFilingType digitalFilingType) {
        digitalFilingType.setName(this.name);
        digitalFilingType.setCode(this.code);
        digitalFilingType.setAutoPurge(this.autoPurge);
        digitalFilingType.setAutoPurgeByDays(this.autoPurgeByDays);
        digitalFilingType.setCsvSeparator(this.csvSeparator);

        // sort request and target indexFields according to seqOrder field
        List<DigitalFilingIndexField> targetDigitalFilingIndexFields = digitalFilingType.getDigitalFilingIndexFields();
        targetDigitalFilingIndexFields.sort(Comparator.comparingInt(DigitalFilingIndexField::getSeqOrder));
        indexFields.sort(Comparator.comparingInt(BaseIndexFieldRequest::getSeqOrder));

        int i = 0;
        for (; i < indexFields.size(); i++) {
            if (targetDigitalFilingIndexFields.size() > i) {
                indexFields.get(i).to(targetDigitalFilingIndexFields.get(i));

            } else {
                DigitalFilingIndexField indexField = new DigitalFilingIndexField();
                indexField.setDigitalFilingType(digitalFilingType);
                indexFields.get(i).to(indexField);
                digitalFilingType.getDigitalFilingIndexFields().add(indexField);
            }
        }

        int currentSize = targetDigitalFilingIndexFields.size();
        int newSize = indexFields.size();
        for (; i < currentSize; i++) {
            digitalFilingType.getDigitalFilingIndexFields().remove(newSize);
        }
    }

}
