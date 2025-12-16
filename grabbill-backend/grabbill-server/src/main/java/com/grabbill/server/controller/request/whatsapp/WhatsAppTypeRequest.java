package com.grabbill.server.controller.request.whatsapp;

import com.grabbill.core.entity.*;
import com.grabbill.server.controller.request.BaseIndexFieldRequest;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author seez
 */
@Data
public class WhatsAppTypeRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    @Size(max = 255)
    private String code;

    private String csvSeparator;

    private boolean passwordProtected;

    private boolean hasAttachment;

    private List<BaseIndexFieldRequest> indexFields = new ArrayList<>();

    private String whatsAppTemplateName;

    private List<WhatsappTemplateParamRequest> whatsAppTemplateParams = new ArrayList<>();


    public void to(final WhatsAppType type) {
        type.setName(this.name);
        type.setCode(this.code);
        type.setCsvSeparator(this.csvSeparator);
        type.setPasswordProtected(this.passwordProtected);
        type.setHasAttachment(this.hasAttachment);
        type.setWhatsAppTemplateName(this.whatsAppTemplateName);

        // sort request and target indexFields according to seqOrder field
        List<WhatsAppIndexField> targetIndexFields = type.getWhatsAppIndexFields();
        targetIndexFields.sort(Comparator.comparingInt(WhatsAppIndexField::getSeqOrder));
        indexFields.sort(Comparator.comparingInt(BaseIndexFieldRequest::getSeqOrder));

        int i = 0;
        for (; i < indexFields.size(); i++) {
            if (targetIndexFields.size() > i) {
                indexFields.get(i).to(targetIndexFields.get(i));

            } else {
                WhatsAppIndexField indexField = new WhatsAppIndexField();
                indexField.setWhatsAppType(type);
                indexFields.get(i).to(indexField);
                type.getWhatsAppIndexFields().add(indexField);
            }
        }

        int currentSize = targetIndexFields.size();
        int newSize = indexFields.size();
        for (; i < currentSize; i++) {
            type.getWhatsAppIndexFields().remove(newSize);
        }

        List<WhatsappTemplateParam> targetWhatsappTemplateParams =
                type.getWhatsappTemplateParams();
        int j = 0;
        for (; j < whatsAppTemplateParams.size(); j++) {
            if (targetWhatsappTemplateParams.size() > j) {
                whatsAppTemplateParams.get(j).to(targetWhatsappTemplateParams.get(j));

            } else {
                WhatsappTemplateParam param = new WhatsappTemplateParam();
                whatsAppTemplateParams.get(j).to(param);
                type.getWhatsappTemplateParams().add(param);
            }
        }

        int currentParamSize = targetWhatsappTemplateParams.size();
        int newParamSize = whatsAppTemplateParams.size();
        for (; j < currentParamSize; j++) {
            type.getWhatsappTemplateParams().remove(newParamSize);
        }
    }

}
