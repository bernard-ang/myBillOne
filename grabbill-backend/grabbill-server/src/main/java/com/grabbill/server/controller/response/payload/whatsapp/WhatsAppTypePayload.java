package com.grabbill.server.controller.response.payload.whatsapp;

import com.grabbill.core.entity.*;
import com.grabbill.server.controller.response.payload.BaseIndexFieldPayload;
import com.grabbill.server.controller.response.payload.BaseTypePayload;
import com.grabbill.server.controller.response.payload.WhatsappTemplateParamPayload;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author michaellow
 */
@Data
public class WhatsAppTypePayload extends BaseTypePayload {

    private boolean passwordProtected;
    private boolean hasAttachment;
    private String csvSeparator;

    private int total;
    private int statusSent;
    private int statusSkip;
    private int statusDelivered;
    private int statusRead;
    private int statusAcknowledge;

    private String whatsAppTemplateName;
    List<WhatsappTemplateParamPayload> whatsAppTemplateParams;


    public static WhatsAppTypePayload from(
            final WhatsAppType type,
            final List<WhatsAppActivity> activities
    ) {
        WhatsAppTypePayload instance = new WhatsAppTypePayload();
        instance.setId(type.getId());
        instance.copyFrom(type);
        instance.setPasswordProtected(type.isPasswordProtected());
        instance.setHasAttachment(type.isHasAttachment());
        instance.setCsvSeparator(type.getCsvSeparator());
        instance.setWhatsAppTemplateName(type.getWhatsAppTemplateName());

        if (activities != null && !activities.isEmpty()) {
            int noOfFiles = 0;
            int total = 0;
            int statusSent = 0;
            int statusSkip = 0;
            int statusDelivered = 0;
            int statusRead = 0;
            int statusAcknowledge = 0;
            for (WhatsAppActivity activity : activities) {
                noOfFiles += activity.getWhatsAppFiles().size();

                total += activity.getWhatsAppIndexRows().size();
                statusSent += activity.getWhatsAppStatusSent();
                statusSkip += activity.getWhatsAppStatusSkip();
                statusDelivered += activity.getWhatsAppStatusDelivered();
                statusRead += activity.getWhatsAppStatusRead();
                statusAcknowledge += activity.getWhatsAppStatusAcknowledge();
            }

            instance.setNoOfFiles(noOfFiles);
            instance.setTotal(total);
            instance.setStatusSent(statusSent);
            instance.setStatusDelivered(statusDelivered);
            instance.setStatusSkip(statusSkip);
            instance.setStatusRead(statusRead);
            instance.setStatusAcknowledge(statusAcknowledge);
        }

        List<BaseIndexFieldPayload> indexFields = new ArrayList<>();
        for (WhatsAppIndexField indexField : type.getWhatsAppIndexFields()) {
            BaseIndexFieldPayload indexFieldPayload = new BaseIndexFieldPayload();
            indexFieldPayload.setId(indexField.getId());
            indexFieldPayload.copyFrom(indexField);
            indexFields.add(indexFieldPayload);
        }
        instance.setIndexFields(indexFields);


        List<WhatsappTemplateParamPayload> params = new ArrayList<>();
        for (WhatsappTemplateParam templateParams : type.getWhatsappTemplateParams()) {
            WhatsappTemplateParamPayload paramPayload = new WhatsappTemplateParamPayload();
            paramPayload.setId(templateParams.getId());
            paramPayload.copyFrom(templateParams);
            params.add(paramPayload);
        }
        instance.setWhatsAppTemplateParams(params);

        return instance;
    }

}
