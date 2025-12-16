package com.grabbill.engine.service;

import javax.mail.BodyPart;
import javax.mail.internet.MimeMultipart;
import java.util.Set;

/**
 * @author michaellow
 */
public interface DsnBodyPartHelper {

    String MESSAGE_ID_HEADER = "message-id:";
    String PLAIN_TEXT = "text/plain";
    String MESSAGE_DELIVERY_STATUS = "message/delivery-status";
    String MESSAGE_RFC822 = "message/rfc822";
    String TEXT_RFC822_HEADERS = "text/rfc822-headers";


    Set<BodyPart> extractBodyParts(MimeMultipart mimeMultipart) throws DsnReaderException;

    String getMessageId(BodyPart bodyPart) throws DsnReaderException;

    String getStatusCode(BodyPart bodyPart) throws DsnReaderException;

    String getReason(BodyPart bodyPart) throws DsnReaderException;

}
