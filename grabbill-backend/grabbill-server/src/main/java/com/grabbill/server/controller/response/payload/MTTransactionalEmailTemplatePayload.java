package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.MTTransactionalEmailTemplate;
import lombok.Data;

/**
 * @author michaellow
 */
@Data
public class MTTransactionalEmailTemplatePayload {

    private String emailTemplateName;
    private String emailSubject;
    private String emailContent;
    private String emailMjmlContent;


    public static MTTransactionalEmailTemplatePayload from(
            final MTTransactionalEmailTemplate mtTransactionalEmailTemplate
    ) {
        MTTransactionalEmailTemplatePayload payload = new MTTransactionalEmailTemplatePayload();
        payload.setEmailTemplateName(mtTransactionalEmailTemplate.getEmailTemplateName());
        payload.setEmailSubject(mtTransactionalEmailTemplate.getEmailSubject());
        payload.setEmailContent(mtTransactionalEmailTemplate.getEmailContent());
        payload.setEmailMjmlContent(mtTransactionalEmailTemplate.getEmailMjmlContent());

        return payload;
    }

}
