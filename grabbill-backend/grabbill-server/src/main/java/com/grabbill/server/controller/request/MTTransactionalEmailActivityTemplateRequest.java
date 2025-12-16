package com.grabbill.server.controller.request;

import com.grabbill.core.entity.MTTransactionalEmailActivityTemplate;
import lombok.Data;

import javax.validation.constraints.Size;

/**
 * @author michaellow
 */
@Data
public class MTTransactionalEmailActivityTemplateRequest {

    private String emailTemplateName;

    @Size(max = 255)
    private String emailSubject;

    private String emailContent;

    private String emailMjmlContent;


    public void to(final MTTransactionalEmailActivityTemplate template) {
        template.setEmailTemplateName(this.emailTemplateName);
        template.setEmailSubject(this.emailSubject);
        template.setEmailContent(this.emailContent);
        template.setEmailMjmlContent(this.emailMjmlContent);
    }

}
