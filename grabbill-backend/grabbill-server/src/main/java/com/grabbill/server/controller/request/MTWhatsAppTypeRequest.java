package com.grabbill.server.controller.request;

import com.grabbill.core.entity.*;
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
public class MTWhatsAppTypeRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    @Size(max = 255)
    private String code;

    private String csvSeparator;

    private boolean passwordProtected;

    private boolean hasAttachment;

    private List<BaseIndexFieldRequest> indexFields = new ArrayList<>();

    private List<MTWhatsAppTemplateRequest> whatsAppTemplates = new ArrayList<>();


    public void to(final MTWhatsAppType type) {
        type.setName(this.name);
        type.setCode(this.code);
        type.setCsvSeparator(this.csvSeparator);
        type.setPasswordProtected(this.passwordProtected);
        type.setHasAttachment(this.hasAttachment);

        // sort request and target indexFields according to seqOrder field
        List<MTWhatsAppIndexField> targetIndexFields = type.getMtWhatsAppIndexFields();
        targetIndexFields.sort(Comparator.comparingInt(MTWhatsAppIndexField::getSeqOrder));
        indexFields.sort(Comparator.comparingInt(BaseIndexFieldRequest::getSeqOrder));

        int i = 0;
        for (; i < indexFields.size(); i++) {
            if (targetIndexFields.size() > i) {
                indexFields.get(i).to(targetIndexFields.get(i));

            } else {
                MTWhatsAppIndexField indexField = new MTWhatsAppIndexField();
                indexField.setMtWhatsAppType(type);
                indexFields.get(i).to(indexField);
                type.getMtWhatsAppIndexFields().add(indexField);
            }
        }

        int currentSize = targetIndexFields.size();
        int newSize = indexFields.size();
        for (; i < currentSize; i++) {
            type.getMtWhatsAppIndexFields().remove(newSize);
        }

        List<MTWhatsAppTemplate> targetWhatsAppTemplates = type.getMtWhatsappTemplates();
        int j = 0;
        for (; j < whatsAppTemplates.size(); j++) {
            if (targetWhatsAppTemplates.size() > j) {
                whatsAppTemplates.get(j).to(targetWhatsAppTemplates.get(j));

            } else {
                MTWhatsAppTemplate template = new MTWhatsAppTemplate();
                template.setMtWhatsAppType(type);
                whatsAppTemplates.get(j).to(template);
                type.getMtWhatsappTemplates().add(template);
            }
        }

        int currentParamSize = targetWhatsAppTemplates.size();
        int newParamSize = whatsAppTemplates.size();
        for (; j < currentParamSize; j++) {
            type.getMtWhatsappTemplates().remove(newParamSize);
        }
    }

}
