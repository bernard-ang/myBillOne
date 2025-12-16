package com.grabbill.server.controller.request;

import com.grabbill.core.entity.TransactionalEmailIndexField;
import com.grabbill.core.entity.TransactionalEmailType;
import com.grabbill.core.entity.TransactionalEmailWhatsappTemplateParam;
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
public class TransactionalEmailTypeRequest {

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
    private String emailSubject;

    private String emailContent;

    private String emailMjmlContent;

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


    public void to(final TransactionalEmailType transactionalEmailType) {
        transactionalEmailType.setName(this.name);
        transactionalEmailType.setCode(this.code);
        transactionalEmailType.setCsvSeparator(this.csvSeparator);
        transactionalEmailType.setEmailFrom(this.emailFrom);
        transactionalEmailType.setEmailFromName(this.emailFromName);
        transactionalEmailType.setEmailSubject(this.emailSubject);
        transactionalEmailType.setEmailContent(this.emailContent);
        transactionalEmailType.setEmailMjmlContent(this.emailMjmlContent);
        transactionalEmailType.setEmailAttachmentFileName(this.emailAttachmentFileName);
        transactionalEmailType.setSendSms(this.sendSms);
        transactionalEmailType.setSmsContent(this.smsContent);
        transactionalEmailType.setPasswordProtected(this.passwordProtected);
        transactionalEmailType.setHasAttachment(this.hasAttachment);
        transactionalEmailType.setArchive(this.archive);
        transactionalEmailType.setAutoPurge(this.autoPurge);
        transactionalEmailType.setAutoPurgeByDays(this.autoPurgeByDays);
        transactionalEmailType.setWhatsAppTemplateName(this.whatsAppTemplateName);

        // sort request and target indexFields according to seqOrder field
        List<TransactionalEmailIndexField> targetTransactionalEmailIndexFields = transactionalEmailType.getTransactionalEmailIndexFields();
        targetTransactionalEmailIndexFields.sort(Comparator.comparingInt(TransactionalEmailIndexField::getSeqOrder));
        indexFields.sort(Comparator.comparingInt(BaseIndexFieldRequest::getSeqOrder));

        int i = 0;
        for (; i < indexFields.size(); i++) {
            if (targetTransactionalEmailIndexFields.size() > i) {
                indexFields.get(i).to(targetTransactionalEmailIndexFields.get(i));

            } else {
                TransactionalEmailIndexField indexField = new TransactionalEmailIndexField();
                indexField.setTransactionalEmailType(transactionalEmailType);
                indexFields.get(i).to(indexField);
                transactionalEmailType.getTransactionalEmailIndexFields().add(indexField);
            }
        }

        int currentSize = targetTransactionalEmailIndexFields.size();
        int newSize = indexFields.size();
        for (; i < currentSize; i++) {
            transactionalEmailType.getTransactionalEmailIndexFields().remove(newSize);
        }

        List<TransactionalEmailWhatsappTemplateParam> targetTransactionalEmailWhatsappTemplateParams =
                transactionalEmailType.getTransactionalEmailWhatsappTemplateParams();
        int j = 0;
        for (; j < whatsAppTemplateParams.size(); j++) {
            if (targetTransactionalEmailWhatsappTemplateParams.size() > j) {
                whatsAppTemplateParams.get(j).to(targetTransactionalEmailWhatsappTemplateParams.get(j));

            } else {
                TransactionalEmailWhatsappTemplateParam param = new TransactionalEmailWhatsappTemplateParam();
                param.setTransactionalEmailType(transactionalEmailType);
                whatsAppTemplateParams.get(j).to(param);
                transactionalEmailType.getTransactionalEmailWhatsappTemplateParams().add(param);
            }
        }

        int currentParamSize = targetTransactionalEmailWhatsappTemplateParams.size();
        int newParamSize = whatsAppTemplateParams.size();
        for (; j < currentParamSize; j++) {
            transactionalEmailType.getTransactionalEmailWhatsappTemplateParams().remove(newParamSize);
        }
    }

}
