package com.grabbill.server.controller.request;

import com.grabbill.core.model.ProtocolEncryption;
import lombok.Data;

import javax.validation.constraints.NotBlank;


/**
 * @author michaellow
 */
@Data
public class MailServerUpdateRequest {

    private String smtpHost;

    private int smtpPort;

    private ProtocolEncryption smtpEncryption;

    private String smtpUsername;

    private String smtpPassword;

    @NotBlank
    private String smtpFrom;

    @NotBlank
    private String smtpFromName;

    private String imapHost;

    private int imapPort;

    private ProtocolEncryption imapEncryption;

    private String imapUsername;

    private String imapPassword;

    private Boolean customServer;

}
