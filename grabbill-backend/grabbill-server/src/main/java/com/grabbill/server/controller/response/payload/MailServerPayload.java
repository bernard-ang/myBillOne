package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.MailServer;
import com.grabbill.core.model.ProtocolEncryption;
import com.grabbill.server.controller.response.ApiPayload;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author michaellow
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MailServerPayload implements ApiPayload {

    private String smtpHost;

    private int smtpPort;

    private ProtocolEncryption smtpEncryption;

    private String smtpUsername;

    private String smtpPassword;

    private String smtpFrom;

    private String smtpFromName;

    private String imapHost;

    private int imapPort;

    private ProtocolEncryption imapEncryption;

    private String imapUsername;

    private String imapPassword;

    private Boolean customServer;


    public static MailServerPayload from(final MailServer mailServer) {
        MailServerPayload instance = new MailServerPayload();
        instance.setCustomServer(mailServer.isCustomServer());

        instance.setSmtpHost(mailServer.getSmtpHost());
        instance.setSmtpPort(mailServer.getSmtpPort());
        instance.setSmtpEncryption(mailServer.getSmtpEncryption());
        instance.setSmtpUsername(mailServer.getSmtpUsername());
        instance.setSmtpPassword(mailServer.getSmtpPassword());
        instance.setSmtpFrom(mailServer.getSmtpFrom());
        instance.setSmtpFromName(mailServer.getSmtpFromName());

        instance.setImapHost(mailServer.getImapHost());
        instance.setImapPort(mailServer.getImapPort());
        instance.setImapEncryption(mailServer.getImapEncryption());
        instance.setImapUsername(mailServer.getImapUsername());
        instance.setImapPassword(mailServer.getImapPassword());

        return instance;
    }

}
