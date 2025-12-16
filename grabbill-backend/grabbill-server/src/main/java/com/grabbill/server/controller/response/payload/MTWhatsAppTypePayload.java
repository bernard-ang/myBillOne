package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.*;
import com.grabbill.core.repository.MTWhatsAppFileRepository;
import com.grabbill.core.repository.MTWhatsAppIndexRowRepository;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author michaellow
 */
@Data
public class MTWhatsAppTypePayload extends BaseTypePayload {

    private boolean passwordProtected;
    private boolean hasAttachment;
    private String csvSeparator;

    private int total;
    private int statusSent;
    private int statusSkip;
    private int statusDelivered;
    private int statusRead;
    private int statusAcknowledge;
    private int statusFailed;

    List<MTWhatsAppTemplatePayload> whatsAppTemplates;


    public static MTWhatsAppTypePayload from(
            final MTWhatsAppType type,
            final List<MTWhatsAppActivity> activities,
            final MTWhatsAppFileRepository mtWhatsAppFileRepository,
            final MTWhatsAppIndexRowRepository mtWhatsAppIndexRowRepository
            ) {
        MTWhatsAppTypePayload instance = new MTWhatsAppTypePayload();
        instance.setId(type.getId());
        instance.copyFrom(type);
        instance.setPasswordProtected(type.isPasswordProtected());
        instance.setHasAttachment(type.isHasAttachment());
        instance.setCsvSeparator(type.getCsvSeparator());

        if (activities != null && !activities.isEmpty()) {
            int noOfFiles = 0;
            int total = 0;
            int statusSent = 0;
            int statusSkip = 0;
            int statusDelivered = 0;
            int statusRead = 0;
            int statusAcknowledge = 0;
            int statusFailed = 0;

            int batchSize = 500;
            for (int i = 0; i < activities.size(); i += batchSize) {
                int end = Math.min(i + batchSize, activities.size());
                List<MTWhatsAppActivity> sublist = activities.subList(i, end);
                noOfFiles += mtWhatsAppFileRepository.countByMtWhatsAppActivityIn(sublist);
                total += mtWhatsAppIndexRowRepository.countByMtWhatsAppActivityIn(sublist);
            }

            for (MTWhatsAppActivity activity : activities) {
                statusSent += activity.getWhatsAppStatusSent();
                statusSkip += activity.getWhatsAppStatusSkip();
                statusDelivered += activity.getWhatsAppStatusDelivered();
                statusRead += activity.getWhatsAppStatusRead();
                statusAcknowledge += activity.getWhatsAppStatusAcknowledge();
                statusFailed += activity.getWhatsAppStatusFailed();
            }

            instance.setNoOfFiles(noOfFiles);
            instance.setTotal(total);
            instance.setStatusSent(statusSent);
            instance.setStatusDelivered(statusDelivered);
            instance.setStatusSkip(statusSkip);
            instance.setStatusRead(statusRead);
            instance.setStatusAcknowledge(statusAcknowledge);
            instance.setStatusFailed(statusFailed);
        }

        List<BaseIndexFieldPayload> indexFields = new ArrayList<>();
        for (MTWhatsAppIndexField indexField : type.getMtWhatsAppIndexFields()) {
            BaseIndexFieldPayload indexFieldPayload = new BaseIndexFieldPayload();
            indexFieldPayload.setId(indexField.getId());
            indexFieldPayload.copyFrom(indexField);
            indexFields.add(indexFieldPayload);
        }
        instance.setIndexFields(indexFields);


        List<MTWhatsAppTemplatePayload> templates = new ArrayList<>();
        for (MTWhatsAppTemplate mtWhatsappTemplate : type.getMtWhatsappTemplates()) {
            templates.add(MTWhatsAppTemplatePayload.from(mtWhatsappTemplate));
        }
        instance.setWhatsAppTemplates(templates);

        return instance;
    }

}
