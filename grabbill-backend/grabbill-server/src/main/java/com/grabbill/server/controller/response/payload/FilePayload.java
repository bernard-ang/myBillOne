package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.*;
import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

/**
 * @author michaellow
 */
@Data
public class FilePayload implements ApiPayload {

    private Long id;

    private String name;

    private Long fileSize;

    private String fileType;


    public static FilePayload from (final DigitalFilingFile digitalFilingFile) {
        FilePayload instance = new FilePayload();
        instance.setId(digitalFilingFile.getId());
        instance.setName(digitalFilingFile.getName());
        instance.setFileSize(digitalFilingFile.getFileSize());
        instance.setFileType(digitalFilingFile.getFileType());

        return instance;
    }

    public static FilePayload from (final TransactionalEmailFile transactionalEmailFile) {
        FilePayload instance = new FilePayload();
        instance.setId(transactionalEmailFile.getId());
        instance.setName(transactionalEmailFile.getName());
        instance.setFileSize(transactionalEmailFile.getFileSize());
        instance.setFileType(transactionalEmailFile.getFileType());

        return instance;
    }

    public static FilePayload from (final MTTransactionalEmailFile mtTransactionalEmailFile) {
        FilePayload instance = new FilePayload();
        instance.setId(mtTransactionalEmailFile.getId());
        instance.setName(mtTransactionalEmailFile.getName());
        instance.setFileSize(mtTransactionalEmailFile.getFileSize());
        instance.setFileType(mtTransactionalEmailFile.getFileType());

        return instance;
    }

    public static FilePayload from (final EmailCampaignFile emailCampaignFile) {
        FilePayload instance = new FilePayload();
        instance.setId(emailCampaignFile.getId());
        instance.setName(emailCampaignFile.getName());
        instance.setFileSize(emailCampaignFile.getFileSize());
        instance.setFileType(emailCampaignFile.getFileType());

        return instance;
    }

    public static FilePayload from (final WhatsAppFile whatsAppFile) {
        FilePayload instance = new FilePayload();
        instance.setId(whatsAppFile.getId());
        instance.setName(whatsAppFile.getName());
        instance.setFileSize(whatsAppFile.getFileSize());
        instance.setFileType(whatsAppFile.getFileType());

        return instance;
    }

    public static FilePayload from (final MTWhatsAppFile mtWhatsAppFile) {
        FilePayload instance = new FilePayload();
        instance.setId(mtWhatsAppFile.getId());
        instance.setName(mtWhatsAppFile.getName());
        instance.setFileSize(mtWhatsAppFile.getFileSize());
        instance.setFileType(mtWhatsAppFile.getFileType());

        return instance;
    }

}
