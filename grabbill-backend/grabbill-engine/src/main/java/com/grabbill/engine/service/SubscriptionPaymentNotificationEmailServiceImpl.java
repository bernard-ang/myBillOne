package com.grabbill.engine.service;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import com.grabbill.core.entity.Invoice;
import com.grabbill.core.exception.GrabbillException;
import com.grabbill.core.model.InvoiceStatus;
import com.grabbill.core.service.InvoiceService;
import com.grabbill.core.service.SmtpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author michaellow
 */
@Slf4j
public class SubscriptionPaymentNotificationEmailServiceImpl implements SubscriptionPaymentNotificationEmailService {

    @Value("${spring.mail.username}")
    private String sender;

    @Value("${email-template.successful-payment.subject}")
    private String successfulPaymentEmailSubject;

    @Value("${email-template.failed-payment.subject}")
    private String failedPaymentEmailSubject;

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private SmtpService smtpService;


    @Override
    public void sendEmail(
            final Invoice invoice,
            final com.stripe.model.Invoice stripeInvoice
    ) {
        MimeMessage message = smtpService.createMimeMessage();

        if (InvoiceStatus.PAID.equals(invoice.getStatus())) {
            sendPaymentSuccessfulEmail(invoice, stripeInvoice, message);

        } else if (InvoiceStatus.PAYMENT_FAILED.equals(invoice.getStatus())) {
            sendPaymentFailedEmail(invoice, message);

        } else {
            log.warn("Payment notification email not sent for invoice [" + invoice.getInvoiceNo() + "] due to invalid status - " + invoice.getStatus());
        }
    }

    private void sendPaymentSuccessfulEmail(
            final Invoice invoice,
            final com.stripe.model.Invoice stripeInvoice,
            final MimeMessage message
    ) {
        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put(NAME, invoice.getBillToName());
        parameterMap.put(PLAN, invoice.getPlanName());
        parameterMap.put(TOTAL_AMOUNT_WITH_TAX, invoice.getTotalAmountWithTax().toString());
        parameterMap.put(INVOICE_NO, invoice.getInvoiceNo());
        parameterMap.put(START_DATE, DateTimeFormatter.ISO_LOCAL_DATE.format(invoice.getCycleStartDate()));
        parameterMap.put(END_DATE, DateTimeFormatter.ISO_LOCAL_DATE.format(invoice.getCycleEndDate()));

        try {
            TemplateLoader loader = new ClassPathTemplateLoader("/templates", ".html");
            Handlebars handlebars = new Handlebars(loader);
            Template template = handlebars.compile("subsequent-payment-success-template");

            message.addFrom(InternetAddress.parse(sender));
            message.setRecipients(Message.RecipientType.TO, invoice.getBillToEmail());
            message.setSubject(successfulPaymentEmailSubject);

            if (InvoiceStatus.PAID.equals(invoice.getStatus())) {
                String outboundProcessedEmailContent = template.apply(parameterMap);
                BodyPart messageContent = new MimeBodyPart();
                messageContent.setContent(outboundProcessedEmailContent, "text/html; charset=utf-8");

                BodyPart messageAttachment = new MimeBodyPart();
                ByteArrayDataSource dataSource = new ByteArrayDataSource(
                        invoiceService.generateInvoicePdf(invoice, stripeInvoice, null), "application/pdf");
                messageAttachment.setDataHandler(new DataHandler(dataSource));
                messageAttachment.setFileName("invoice.pdf");

                Multipart multipart = new MimeMultipart();
                multipart.addBodyPart(messageContent);
                multipart.addBodyPart(messageAttachment);
                message.setContent(multipart);

            } else {
                message.setContent(template.apply(parameterMap), "text/html");
            }

            smtpService.send(message);

        } catch (IOException | MessagingException e) {
            throw new GrabbillException(
                    "Failed to send new payment successful notification email to - " + invoice.getBillToEmail(), e);
        }
    }

    private void sendPaymentFailedEmail(final Invoice invoice, final MimeMessage message) {
        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put(NAME, invoice.getBillToName());
        parameterMap.put(PLAN, invoice.getPlanName());
        parameterMap.put(TOTAL_AMOUNT_WITH_TAX, invoice.getTotalAmountWithTax().toString());
        parameterMap.put(INVOICE_NO, invoice.getInvoiceNo());
        parameterMap.put(START_DATE, DateTimeFormatter.ISO_LOCAL_DATE.format(invoice.getCycleStartDate()));
        parameterMap.put(END_DATE, DateTimeFormatter.ISO_LOCAL_DATE.format(invoice.getCycleEndDate()));

        try {
            TemplateLoader loader = new ClassPathTemplateLoader("/templates", ".html");
            Handlebars handlebars = new Handlebars(loader);
            Template template = handlebars.compile("subsequent-payment-failed-template");

            message.addFrom(InternetAddress.parse(sender));
            message.setRecipients(Message.RecipientType.TO, invoice.getBillToEmail());
            message.setSubject(failedPaymentEmailSubject);
            message.setContent(template.apply(parameterMap), "text/html");

            smtpService.send(message);

        } catch (IOException | MessagingException e) {
            throw new GrabbillException(
                    "Failed to send subsequent payment failed notification email to - " + invoice.getBillToEmail(), e);
        }
    }

}
