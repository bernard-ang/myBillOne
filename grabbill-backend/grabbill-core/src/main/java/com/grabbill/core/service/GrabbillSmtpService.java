package com.grabbill.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.javamail.JavaMailSender;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.List;

/**
 * @author michaellow
 */
public class GrabbillSmtpService implements SmtpService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private MessageIdGenerator messageIdGenerator;


    @Override
    public MimeMessage createMimeMessage() throws MailException {
        return javaMailSender.createMimeMessage();
    }

    @Override
    public MimeMessage createMimeMessage(final Long indexRowId) throws MailException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        if (indexRowId != null) {
            try {
                mimeMessage.setHeader(HEADER_MESSAGE_ID, messageIdGenerator.generate(indexRowId.toString()));
            } catch (MessagingException e) {
                throw new MailPreparationException(e);
            }
        }

        return mimeMessage;
    }

    @Override
    public void send(final MimeMessage mimeMessage) throws MailException {
        javaMailSender.send(mimeMessage);
    }

    @Override
    public void send(final List<MimeMessage> mimeMessages) throws MailException {
        MimeMessage[] mimeMessagesArray = new MimeMessage[mimeMessages.size()];
        javaMailSender.send(mimeMessages.toArray(mimeMessagesArray));
    }

}
