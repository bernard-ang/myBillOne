package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.*;
import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author michaellow
 */
@Data
public class FilesPayload implements ApiPayload {

    private List<FilePayload> files = new ArrayList<>();


    public static FilesPayload fromDigitalFilingFiles(final List<DigitalFilingFile> digitalFilingFiles) {
        FilesPayload instance = new FilesPayload();

        for (DigitalFilingFile digitalFilingFile : digitalFilingFiles) {
            instance.getFiles().add(FilePayload.from(digitalFilingFile));
        }

        return instance;
    }

    public static FilesPayload fromTransactionalEmailFiles(final List<TransactionalEmailFile> transactionalEmailFiles) {
        FilesPayload instance = new FilesPayload();

        for (TransactionalEmailFile transactionalEmailFile : transactionalEmailFiles) {
            instance.getFiles().add(FilePayload.from(transactionalEmailFile));
        }

        return instance;
    }

    public static FilesPayload fromMtTransactionalEmailFiles(final List<MTTransactionalEmailFile> mtTransactionalEmailFiles) {
        FilesPayload instance = new FilesPayload();

        for (MTTransactionalEmailFile mtTransactionalEmailFile : mtTransactionalEmailFiles) {
            instance.getFiles().add(FilePayload.from(mtTransactionalEmailFile));
        }

        return instance;
    }

    public static FilesPayload fromEmailCampaignFiles(final List<EmailCampaignFile> emailCampaignFiles) {
        FilesPayload instance = new FilesPayload();

        for (EmailCampaignFile emailCampaignFile : emailCampaignFiles) {
            instance.getFiles().add(FilePayload.from(emailCampaignFile));
        }

        return instance;
    }

    public static FilesPayload fromWhatsAppFiles(final List<WhatsAppFile> whatsAppFiles) {
        FilesPayload instance = new FilesPayload();

        for (WhatsAppFile file : whatsAppFiles) {
            instance.getFiles().add(FilePayload.from(file));
        }

        return instance;
    }

    public static FilesPayload fromMtWhatsAppFiles(final List<MTWhatsAppFile> mtWhatsAppFiles) {
        FilesPayload instance = new FilesPayload();

        for (MTWhatsAppFile file : mtWhatsAppFiles) {
            instance.getFiles().add(FilePayload.from(file));
        }

        return instance;
    }

}
