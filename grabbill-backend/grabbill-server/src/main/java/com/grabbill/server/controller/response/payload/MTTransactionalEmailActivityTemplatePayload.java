package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.MTTransactionalEmailActivityTemplate;
import lombok.Data;

/**
 * @author michaellow
 */
@Data
public class MTTransactionalEmailActivityTemplatePayload {

    private String emailTemplateName;
    private String emailSubject;
    private String emailContent;
    private String emailMjmlContent;


    public static MTTransactionalEmailActivityTemplatePayload from(final MTTransactionalEmailActivityTemplate mtTransactionalEmailActivityTemplate) {
        MTTransactionalEmailActivityTemplatePayload instance = new MTTransactionalEmailActivityTemplatePayload();
        instance.setEmailTemplateName(mtTransactionalEmailActivityTemplate.getEmailTemplateName());
        instance.setEmailSubject(mtTransactionalEmailActivityTemplate.getEmailSubject());
        instance.setEmailContent(mtTransactionalEmailActivityTemplate.getEmailContent());
        instance.setEmailMjmlContent(mtTransactionalEmailActivityTemplate.getEmailMjmlContent());

        return instance;
    }

}
