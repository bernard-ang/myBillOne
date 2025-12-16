package com.grabbill.core.service;

import com.grabbill.core.model.ProtocolEncryption;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.MailSendException;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import java.util.*;

/**
 * @author michaellow
 */
@Slf4j
public class CustomSmtpService implements SmtpService {

    private final Properties properties;
    private final String host;
    private final int port;
    private final ProtocolEncryption protocolEncryption;
    private final String username;
    private final String password;
    private Session session;
    private MessageIdGenerator messageIdGenerator;


    private CustomSmtpService(
            final String host,
            final int port,
            final ProtocolEncryption protocolEncryption,
            final String username,
            final String password,
            final MessageIdGenerator messageIdGenerator
    ) {
        this.host = host;
        this.port = port;
        this.protocolEncryption = protocolEncryption;
        this.username = username;
        this.password = password;
        this.messageIdGenerator = messageIdGenerator;
        this.properties = System.getProperties();
    }

    public static SmtpService getInstance(
            final String host,
            final int port,
            final ProtocolEncryption protocolEncryption,
            final String username,
            final String password,
            final MessageIdGenerator messageIdGenerator
    ) {
        return new CustomSmtpService(host, port, protocolEncryption, username, password, messageIdGenerator);
    }

    @Override
    public MimeMessage createMimeMessage() throws MailException {
        throw new UnsupportedOperationException();
    }

    @Override
    public MimeMessage createMimeMessage(final Long indexRowId) throws MailException {
        initSessionIfRequired();

        MimeMessage mimeMessage = new MimeMessage(session);
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
        initSessionIfRequired();

        Map<Object, Exception> failedMessages = sendInternal(Collections.singletonList(mimeMessage));
        if (!failedMessages.isEmpty()) {
            throw new MailSendException(failedMessages);
        }
    }

    @Override
    public void send(final List<MimeMessage> mimeMessages) throws MailException {
        initSessionIfRequired();

        Map<Object, Exception> failedMessages = sendInternal(mimeMessages);
        if (!failedMessages.isEmpty()) {
            throw new MailSendException(failedMessages);
        }
    }

    private Map<Object, Exception> sendInternal(final List<MimeMessage> mimeMessages) {
        Map<Object, Exception> failedMessages = new LinkedHashMap<>();
        Transport transport = null;

        try {
            transport = session.getTransport();
            transport.connect(this.host, this.port, this.username, this.password);

            for (MimeMessage mimeMessage : mimeMessages) {
                try {
                    if (mimeMessage.getSentDate() == null) {
                        mimeMessage.setSentDate(new Date());
                    }

                    String messageId = mimeMessage.getMessageID();
                    mimeMessage.saveChanges();
                    if (messageId != null) {
                        mimeMessage.setHeader(HEADER_MESSAGE_ID, messageId);
                    }
                    Address[] addresses = mimeMessage.getAllRecipients();
                    transport.sendMessage(mimeMessage, (addresses != null ? addresses : new Address[0]));

                } catch (Exception e) {
                    failedMessages.put(mimeMessage, e);
                }
            }

        } catch (MessagingException e) {
            for (MimeMessage mimeMessage : mimeMessages) {
                failedMessages.put(mimeMessage, e);
            }

        } finally {
            if (transport != null) {
                try {
                    transport.close();
                } catch (MessagingException e) {
                    log.warn("Failed to close mail transport", e);
                }
            }
        }

        return failedMessages;
    }

    private void initSessionIfRequired() {
        if (this.session == null) {
            this.properties.put("mail.transport.protocol", "smtp");
            this.properties.put("mail.smtp.host", this.host);
            this.properties.put("mail.smtp.port", this.port);

            Authenticator authenticator = null;
            if (ProtocolEncryption.SSL.equals(this.protocolEncryption)) {
                authenticator = getAuthenticator();
                this.properties.put("mail.smtp.auth", true);
                this.properties.put("mail.smtp.socketFactory.port", this.port);
                this.properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

            } else if (ProtocolEncryption.TLS.equals(this.protocolEncryption)) {
                authenticator = getAuthenticator();
                this.properties.put("mail.smtp.auth", "true");
                this.properties.put("mail.smtp.starttls.enable", true);
                this.properties.put("mail.smtp.ssl.trust", "*");
            }

            this.session = Session.getInstance(this.properties, authenticator);
        }
    }

    private Authenticator getAuthenticator() {
        return new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        };
    }

}
