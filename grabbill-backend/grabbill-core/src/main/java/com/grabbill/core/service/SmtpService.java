package com.grabbill.core.service;

import org.springframework.mail.MailException;

import javax.mail.internet.MimeMessage;
import java.util.List;

/**
 * @author michaellow
 */
public interface SmtpService {

    String HEADER_MESSAGE_ID = "Message-ID";


    MimeMessage createMimeMessage() throws MailException;

    MimeMessage createMimeMessage(Long indexRowId) throws MailException;

    void send(MimeMessage mimeMessage) throws MailException;

    void send(List<MimeMessage> mimeMessages) throws MailException;

}
