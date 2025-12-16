package com.grabbill.server.service;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import com.grabbill.core.entity.Invoice;
import com.grabbill.core.entity.PromoCode;
import com.grabbill.core.exception.GrabbillException;
import com.grabbill.core.model.InvoiceStatus;
import com.grabbill.core.service.InvoiceService;
import com.grabbill.core.service.SmtpService;
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
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author michaellow
 */
public class PaymentNotificationEmailServiceImpl implements PaymentNotificationEmailService {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");

    @Value("${spring.mail.username}")
    private String sender;

    @Value("${email-template.bcc.successful-payment.subject}")
    private String bindCardChargeSuccessfulEmailSubject;

    @Value("${email-template.bcc.failed-payment.subject}")
    private String bindCardChargeFailedEmailSubject;


    @Value("${email-template.sms.sucessful-topup.subject}")
    private String smsCreditsTopupSuccessfulEmailSubject;

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private SmtpService smtpService;


    @Override
    public void sendEmail(
            final Invoice invoice,
            final com.stripe.model.Invoice stripeInvoice,
            final PromoCode promoCode
            ) {
        MimeMessage message = smtpService.createMimeMessage();

        if (InvoiceStatus.PAID.equals(invoice.getStatus())) {
            sendPaymentNotificationEmail(
                    invoice,
                    stripeInvoice,
                    promoCode,
                    message,
                    bindCardChargeSuccessfulEmailSubject,
                    "bcc-success-template"
            );

        } else if (InvoiceStatus.PAYMENT_FAILED.equals(invoice.getStatus())) {
            sendPaymentNotificationEmail(
                    invoice,
                    stripeInvoice,
                    promoCode,
                    message,
                    bindCardChargeFailedEmailSubject,
                    "bcc-failed-template"
            );
        }
    }

    private void sendPaymentNotificationEmail(
            final Invoice invoice,
            final com.stripe.model.Invoice stripeInvoice,
            final PromoCode promoCode,
            final MimeMessage message,
            final String subject,
            final String templateName
    ) {
        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put(NAME, invoice.getBillToName());
        parameterMap.put(PLAN, invoice.getPlanName());
        parameterMap.put(TOTAL_AMOUNT_WITH_TAX, DECIMAL_FORMAT.format(invoice.getTotalAmountWithTax()));
        parameterMap.put(INVOICE_NO, invoice.getInvoiceNo());
        parameterMap.put(START_DATE, DateTimeFormatter.ISO_LOCAL_DATE.format(invoice.getCycleStartDate()));
        parameterMap.put(END_DATE, DateTimeFormatter.ISO_LOCAL_DATE.format(invoice.getCycleEndDate()));

        try {
            TemplateLoader loader = new ClassPathTemplateLoader("/templates", ".html");
            Handlebars handlebars = new Handlebars(loader);
            Template template = handlebars.compile(templateName);

            message.addFrom(InternetAddress.parse(sender));
            message.setRecipients(Message.RecipientType.TO, invoice.getBillToEmail());
            message.setSubject(subject);

            if (InvoiceStatus.PAID.equals(invoice.getStatus())) {
                String outboundProcessedEmailContent = template.apply(parameterMap);
                BodyPart messageContent = new MimeBodyPart();
                messageContent.setContent(outboundProcessedEmailContent, "text/html; charset=utf-8");

                BodyPart messageAttachment = new MimeBodyPart();
                ByteArrayDataSource dataSource = new ByteArrayDataSource(
                        invoiceService.generateInvoicePdf(invoice, stripeInvoice, promoCode), "application/pdf");
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
                    "Failed to send payment notification email to - " + invoice.getBillToEmail(), e);
        }
    }

    @Override
    public void sendCreditsTopupEmail(
            final Invoice invoice,
            final com.stripe.model.Invoice stripeInvoice
    ) {
        MimeMessage message = smtpService.createMimeMessage();

        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put(NAME, invoice.getBillToName());
        parameterMap.put(TOTAL_AMOUNT_WITH_TAX, DECIMAL_FORMAT.format(invoice.getTotalAmountWithTax()));
        parameterMap.put(INVOICE_NO, invoice.getInvoiceNo());

        try {
            TemplateLoader loader = new ClassPathTemplateLoader("/templates", ".html");
            Handlebars handlebars = new Handlebars(loader);
            Template template = handlebars.compile("sms-credits-topup-success");

            message.addFrom(InternetAddress.parse(sender));
            message.setRecipients(Message.RecipientType.TO, invoice.getBillToEmail());
            message.setSubject(smsCreditsTopupSuccessfulEmailSubject);

            String outboundProcessedEmailContent = template.apply(parameterMap);
            BodyPart messageContent = new MimeBodyPart();
            messageContent.setContent(outboundProcessedEmailContent, "text/html; charset=utf-8");

            BodyPart messageAttachment = new MimeBodyPart();
            ByteArrayDataSource dataSource = new ByteArrayDataSource(
                    invoiceService.generateSmsTopupInvoicePdf(invoice, stripeInvoice), "application/pdf");
            messageAttachment.setDataHandler(new DataHandler(dataSource));
            messageAttachment.setFileName("invoice.pdf");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageContent);
            multipart.addBodyPart(messageAttachment);
            message.setContent(multipart);

            smtpService.send(message);

        } catch (IOException | MessagingException e) {
            throw new GrabbillException(
                    "Failed to send sms credits topup notification email to - " + invoice.getBillToEmail(), e);
        }
    }

}
