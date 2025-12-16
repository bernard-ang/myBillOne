package com.grabbill.server.controller.request;

import com.grabbill.core.entity.MTTransactionalEmailIndexField;
import com.grabbill.core.entity.MTTransactionalEmailTemplate;
import com.grabbill.core.entity.MTTransactionalEmailType;
import com.grabbill.core.entity.MTTransactionalEmailWhatsappTemplateParam;
import com.grabbill.server.controller.request.whatsapp.WhatsappTemplateParamRequest;
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
public class MTTransactionalEmailTypeRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    @Size(max = 255)
    private String code;

    private String csvSeparator;

    @Size(max = 255)
    private String emailFrom;

    @Size(max = 255)
    private String emailFromName;

    @Size(max = 255)
    private String emailAttachmentFileName;

    private boolean sendSms;

    @Size(max = 255)
    private String smsContent;

    private boolean passwordProtected;

    private boolean hasAttachment;

    private boolean archive;

    private boolean autoPurge;

    private Integer autoPurgeByDays;

    private List<BaseIndexFieldRequest> indexFields = new ArrayList<>();

    private String whatsAppTemplateName;

    private List<WhatsappTemplateParamRequest> whatsAppTemplateParams = new ArrayList<>();

    private List<MTTransactionalEmailTemplateRequest> transactionalEmailTemplates = new ArrayList<>();


    public void to(final MTTransactionalEmailType mtTransactionalEmailType) {
        mtTransactionalEmailType.setName(this.name);
        mtTransactionalEmailType.setCode(this.code);
        mtTransactionalEmailType.setCsvSeparator(this.csvSeparator);
        mtTransactionalEmailType.setEmailFrom(this.emailFrom);
        mtTransactionalEmailType.setEmailFromName(this.emailFromName);
        mtTransactionalEmailType.setEmailAttachmentFileName(this.emailAttachmentFileName);
        mtTransactionalEmailType.setSendSms(this.sendSms);
        mtTransactionalEmailType.setSmsContent(this.smsContent);
        mtTransactionalEmailType.setPasswordProtected(this.passwordProtected);
        mtTransactionalEmailType.setHasAttachment(this.hasAttachment);
        mtTransactionalEmailType.setArchive(this.archive);
        mtTransactionalEmailType.setAutoPurge(this.autoPurge);
        mtTransactionalEmailType.setAutoPurgeByDays(this.autoPurgeByDays);
        mtTransactionalEmailType.setWhatsAppTemplateName(this.whatsAppTemplateName);

        // sort request and target indexFields according to seqOrder field
        List<MTTransactionalEmailIndexField> targetTransactionalEmailIndexFields = mtTransactionalEmailType.getMtTransactionalEmailIndexFields();
        targetTransactionalEmailIndexFields.sort(Comparator.comparingInt(MTTransactionalEmailIndexField::getSeqOrder));
        indexFields.sort(Comparator.comparingInt(BaseIndexFieldRequest::getSeqOrder));

        int i = 0;
        for (; i < indexFields.size(); i++) {
            if (targetTransactionalEmailIndexFields.size() > i) {
                indexFields.get(i).to(targetTransactionalEmailIndexFields.get(i));

            } else {
                MTTransactionalEmailIndexField indexField = new MTTransactionalEmailIndexField();
                indexField.setMtTransactionalEmailType(mtTransactionalEmailType);
                indexFields.get(i).to(indexField);
                mtTransactionalEmailType.getMtTransactionalEmailIndexFields().add(indexField);
            }
        }

        int currentSize = targetTransactionalEmailIndexFields.size();
        int newSize = indexFields.size();
        for (; i < currentSize; i++) {
            mtTransactionalEmailType.getMtTransactionalEmailIndexFields().remove(newSize);
        }

        List<MTTransactionalEmailWhatsappTemplateParam> targetTransactionalEmailWhatsappTemplateParams =
                mtTransactionalEmailType.getMtTransactionalEmailWhatsappTemplateParams();
        int j = 0;
        for (; j < whatsAppTemplateParams.size(); j++) {
            if (targetTransactionalEmailWhatsappTemplateParams.size() > j) {
                whatsAppTemplateParams.get(j).to(targetTransactionalEmailWhatsappTemplateParams.get(j));

            } else {
                MTTransactionalEmailWhatsappTemplateParam param = new MTTransactionalEmailWhatsappTemplateParam();
                param.setMtTransactionalEmailType(mtTransactionalEmailType);
                whatsAppTemplateParams.get(j).to(param);
                mtTransactionalEmailType.getMtTransactionalEmailWhatsappTemplateParams().add(param);
            }
        }

        int currentParamSize = targetTransactionalEmailWhatsappTemplateParams.size();
        int newParamSize = whatsAppTemplateParams.size();
        for (; j < currentParamSize; j++) {
            mtTransactionalEmailType.getMtTransactionalEmailWhatsappTemplateParams().remove(newParamSize);
        }

        List<MTTransactionalEmailTemplate> targetTransactionalEmailTemplates =
                mtTransactionalEmailType.getMtTransactionalEmailTemplates();
        int k = 0;
        for (; k < transactionalEmailTemplates.size(); k++) {
            if (targetTransactionalEmailTemplates.size() > k) {
                transactionalEmailTemplates.get(k).to(targetTransactionalEmailTemplates.get(k));

            } else {
                MTTransactionalEmailTemplate template = new MTTransactionalEmailTemplate();
                template.setMtTransactionalEmailType(mtTransactionalEmailType);
                transactionalEmailTemplates.get(k).to(template);
                mtTransactionalEmailType.getMtTransactionalEmailTemplates().add(template);
            }
        }

        int currentTemplateSize = targetTransactionalEmailTemplates.size();
        int newTemplateSize = transactionalEmailTemplates.size();
        for (; k < currentTemplateSize; k++) {
            mtTransactionalEmailType.getMtTransactionalEmailTemplates().remove(newTemplateSize);
        }
    }

}
