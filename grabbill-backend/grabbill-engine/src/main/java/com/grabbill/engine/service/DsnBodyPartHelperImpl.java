package com.grabbill.engine.service;

import lombok.Data;
import org.apache.commons.compress.utils.IOUtils;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;

/**
 * @author michaellow
 */
@Data
public class DsnBodyPartHelperImpl implements DsnBodyPartHelper {

    @Override
    public Set<BodyPart> extractBodyParts(final MimeMultipart mimeMultipart) throws DsnReaderException {
        try {
            Set<BodyPart> bodyParts = new HashSet<>();

            int count = 0;
            while (count < mimeMultipart.getCount()) {

                BodyPart bodyPart = mimeMultipart.getBodyPart(count);
                if (bodyPart.getContent() instanceof MimeMultipart) {
                    MimeMultipart mimeMultipartBodyPart = (MimeMultipart) bodyPart.getContent();
                    bodyParts.addAll(extractBodyParts(mimeMultipartBodyPart));

                } else {
                    bodyParts.add(bodyPart);
                }

                count++;
            }

            return bodyParts;

        } catch (IOException | MessagingException e) {
            throw new DsnReaderException("Failed to retrieve all BodyParts from given Mime Multipart", e);
        }
    }

    @Override
    public String getMessageId(final BodyPart bodyPart) throws DsnReaderException {

        try {
            String contentType = bodyPart.getContentType();
            if (contentType.toLowerCase().startsWith(MESSAGE_RFC822)
                    || contentType.toLowerCase().startsWith(TEXT_RFC822_HEADERS)) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(bodyPart.getInputStream()));
                String messageId = null;

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    line = line.trim();

                    if (line.toLowerCase().startsWith(MESSAGE_ID_HEADER)) {
                        messageId = line.substring(12);
                        break;
                    }
                }

                return messageId;
            }

            throw new DsnReaderException("Failed to read messageID from given bodyPart of type - " + contentType);

        } catch (IOException | MessagingException e) {
            throw new DsnReaderException("Failed to read messageID from given bodypart", e);
        }
    }

    @Override
    public String getStatusCode(final BodyPart bodyPart) throws DsnReaderException {
        try {
            String contentType = bodyPart.getContentType();
            if (contentType.toLowerCase().startsWith(MESSAGE_DELIVERY_STATUS)) {

                String statusCode = null;

                String message = new String(IOUtils.toByteArray(bodyPart.getInputStream()));
                Matcher rfc1893Matcher = RFCPatterns.RFC1893_PATTERN.matcher(message);
                if (rfc1893Matcher.find()) {
                    statusCode = rfc1893Matcher.group(rfc1893Matcher.groupCount());

                } else {
                    Matcher rfc2821Matcher = RFCPatterns.RFC2821_PATTERN.matcher(message);
                    if (rfc2821Matcher.find()) {
                        statusCode = rfc2821Matcher.group(rfc2821Matcher.groupCount());
                    }
                }

                if (statusCode != null) {
                    return statusCode;
                }
            }

            throw new DsnReaderException("Failed to read status code from given bodyPart of type - " + contentType);

        } catch (IOException | MessagingException e) {
            throw new DsnReaderException("Failed to read status code from given bodypart", e);
        }
    }

    @Override
    public String getReason(final BodyPart bodyPart) throws DsnReaderException {
        try {
            String contentType = bodyPart.getContentType();
            if (contentType.toLowerCase().startsWith(PLAIN_TEXT)) {
                return (String) bodyPart.getContent();
            }

            throw new DsnReaderException("Failed to read reason from given bodyPart of type - " + contentType);

        } catch (IOException | MessagingException e) {
            throw new DsnReaderException("Failed to read reason from given bodypart", e);
        }
    }

}
