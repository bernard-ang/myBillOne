package com.grabbill.server.controller;

import com.grabbill.core.entity.MTWhatsAppActivity;
import com.grabbill.core.entity.MTWhatsAppType;
import com.grabbill.core.model.whatsapp.WhatsAppConstants;
import com.grabbill.core.model.whatsapp.components.FooterComponent;
import com.grabbill.core.model.whatsapp.request.Sample;
import com.grabbill.core.model.whatsapp.request.TemplateRequest;
import com.grabbill.core.model.whatsapp.components.BodyComponent;
import com.grabbill.core.model.whatsapp.components.HeaderComponent;
import com.grabbill.core.model.whatsapp.request.enums.Category;
import com.grabbill.core.model.whatsapp.request.enums.ComponentFormat;
import com.grabbill.core.model.whatsapp.request.enums.Language;
import com.grabbill.core.model.whatsapp.response.AuthenticateBlastResponse;
import com.grabbill.core.model.whatsapp.response.RefreshTemplateResponse;
import com.grabbill.core.model.whatsapp.response.SendMessageResponse;
import com.grabbill.core.model.whatsapp.response.ThirdPartyWebhookResponse;
import com.grabbill.core.service.BaseActivityService;
import com.grabbill.core.service.MTWhatsAppActivityServiceImpl;
import com.grabbill.core.service.whatsapp.WhatsAppService;
import com.grabbill.core.service.whatsapp.WhatsAppSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

/**
 * login                       : login to WA account
 * get-templates               : get registered templates
 * create-text-template        : create simple text based template
 * create-document-template    : create document (i.e. PDF) based template
 * create-placeholder-template : create templates with placeholders (i.e. based on index fields)
 * delete-template             : delete template
 * update-template             : unsupported. not supported by Nubitel.
 * send-text-message           : send (simple) template message
 * send-document-message       : send template message with PDF
 * send-placeholder-message    : send template message with placeholders
 * send-reply-message          : send message without template (e.g. reply user messages)
 * register-webhook            : register webhook to get notifications (messaging send, delivery status, user reply messages)
 * unregister-webhook          : unsupported. TODO: Nubitel says this is supported, API reference provided by Ros. need to implement
 * hook                        : endpoint to actually receive notifications from WhatsApp/Nubitel.
 */
@Slf4j
@RestController
@RequestMapping("/wa-sandbox")
public class WhatsAppSandboxController {
    private String wabaGuid = "a3793d4f-3b21-461c-97b5-5369588f1611";
    private String email = "mybillone@nubitel.co";
    private String password = "23w1LLch4!";

    // christine
    // private String testPhoneNo = "60123981688";
    // kc
    private String testPhoneNo = "60129729779";

    private final WhatsAppService service;

    private final BaseActivityService<MTWhatsAppType, MTWhatsAppActivity> mtWhatsAppActivityService;
    private WhatsAppSession session;

    public WhatsAppSandboxController(WhatsAppService service,
                                     @Qualifier("mtWhatsAppActivityService")
                                     BaseActivityService<MTWhatsAppType, MTWhatsAppActivity> mtWhatsAppActivityService) {
        this.service = service;
        this.mtWhatsAppActivityService = mtWhatsAppActivityService;
    }

    @GetMapping("ping")
    ResponseEntity<String> ping() {
        return ResponseEntity.ok("ok");
    }

    @GetMapping("update-activities")
    ResponseEntity<String> updateActivities() {
        List<MTWhatsAppActivity> allProcessedActivities = mtWhatsAppActivityService.getAllProcessedActivities();
        log.info("Updating [{}] activities", allProcessedActivities.size());
        for (MTWhatsAppActivity mtWhatsAppActivity : allProcessedActivities) {
            MTWhatsAppActivity updatedActivity = mtWhatsAppActivityService.updateCount(mtWhatsAppActivity);
            mtWhatsAppActivityService.save(updatedActivity);
            log.info("Updated activity [{}]", updatedActivity.getId());
        }

        return ResponseEntity.ok("updated");
    }

    @GetMapping("login")
    ResponseEntity<AuthenticateBlastResponse> login() {
        return ResponseEntity.ok(getSession().getAuthInfo());
    }

    @GetMapping("get-templates")
    ResponseEntity<List<RefreshTemplateResponse>> getTemplates() {
        return ResponseEntity.ok(getSession().refreshTemplate());
    }

    @GetMapping("create-text-template")
    ResponseEntity<RefreshTemplateResponse> createTextTemplate() {
        TemplateRequest template = TemplateRequest.builder()
                .name("mybillone_stmt_text_2")
                .language(Language.ENGLISH)
                .category(Category.UTILITY)
                .components(List.of(BodyComponent.builder()
                        .text("Your utility statement is ready. Please review on website, thank you!").build()
                ))
                .build();

        return ResponseEntity.ok(getSession().createTemplate(template));
    }

    @GetMapping("create-document-template")
    ResponseEntity<RefreshTemplateResponse> createDocumentTemplate() throws IOException {
        byte[] samplePdf = getClass().getResourceAsStream("/samples/sample.pdf").readAllBytes();

        TemplateRequest template = TemplateRequest.builder()
                .name("mybillone_stmt_document_t1")
                .language(Language.ENGLISH)
                .category(Category.UTILITY)
                .components(
                        List.of(
                                HeaderComponent.builder()
                                        .format(ComponentFormat.DOCUMENT).build(),
                                BodyComponent.builder()
                                        .text("Your utility statement is ready. Please review on {{1}} website, {{2}} thank you!").build()
                        ))
                .sample(Sample.builder()
                        .fileData(Base64.getEncoder().encodeToString(samplePdf))
                        .fileSize(samplePdf.length)
                        .fileMimeType("application/pdf")
                        .build())
                .build();

        return ResponseEntity.ok(getSession().createTemplate(template));
    }

    @GetMapping("create-sunlife-template")
    ResponseEntity<RefreshTemplateResponse> createSunlifeDocumentTemplate() throws IOException {
        byte[] samplePdf = getClass().getResourceAsStream("/samples/sunlife-policy.pdf").readAllBytes();

        TemplateRequest template = TemplateRequest.builder()
                .name("mybillone_dev_sunlife_en")
                .language(Language.ENGLISH)
                .category(Category.UTILITY)
                .components(
                        List.of(
                                HeaderComponent.builder()
                                        .format(ComponentFormat.DOCUMENT).build(),
                                BodyComponent.builder()
                                        .text("Your e-{{1}} is ready for viewing – unlock a brighter financial future with Sun Life Malaysia.\n" +
                                                "\n" +
                                                "Dear {{2}},\n" +
                                                "\n" +
                                                "Your document is now available for viewing. To access it, simply enter your *Year and Month of Birth* in a YYYYMMM format, followed by the *last 4 digits of your NRIC number*. *Example: 1983JUL5688*\n" +
                                                "\n" +
                                                "You can also check your plan details anytime, anywhere, via the SunAccess Client portal.\n" +
                                                "\n" +
                                                "To secure you and your loved ones’ financial future, please review the plan to ensure it aligns with your needs. Don’t forget to keep a copy for future claims purposes.\n" +
                                                "\n" +
                                                "For enquires, please call our Client Careline at *1300-88-5055* or email us at *wecare@sunlifemalaysia.com*.\n" +
                                                "\n" +
                                                "Thank you for choosing Sun Life Malaysia. We wish you brighter days ahead!").build(),
                                FooterComponent.builder()
                                        .text("This is an auto generated data").build()
                        ))
                .sample(Sample.builder()
                        .fileData(Base64.getEncoder().encodeToString(samplePdf))
                        .fileSize(samplePdf.length)
                        .fileMimeType("application/pdf")
                        .build())
                .build();

        return ResponseEntity.ok(getSession().createTemplate(template));
    }

    @GetMapping("create-placeholder-template")
    ResponseEntity<RefreshTemplateResponse> createPlaceholderTemplate() throws IOException {
        byte[] samplePdf = getClass().getResourceAsStream("/samples/sample.pdf").readAllBytes();

        TemplateRequest template = TemplateRequest.builder()
                .name("mybillone_stmt_placeholder_3")
                .language(Language.ENGLISH)
                .category(Category.UTILITY)
                .components(List.of(
                        HeaderComponent.builder()
                                .format(ComponentFormat.DOCUMENT).build(),
                        BodyComponent.builder()
                                .text("Your utility statement {{1}} is ready. Please review on website, thank you! Contact us at {{2}} for help.").build()
                ))
                .sample(Sample.builder()
                        .fileData(Base64.getEncoder().encodeToString(samplePdf))
                        .fileSize(samplePdf.length)
                        .fileMimeType("application/pdf")
                        .build())
                .build();

        return ResponseEntity.ok(getSession().createTemplate(template));
    }

    @GetMapping("delete-template")
    ResponseEntity<String> deleteTemplate(@RequestParam("id") String id) {
        return ResponseEntity.ok(getSession().deleteTemplate(id));
    }

    @GetMapping("send-text-message")
    ResponseEntity<SendMessageResponse> sendTextMessage() {
        return ResponseEntity.ok(getSession().sendTemplateMessage("welcome", this.testPhoneNo));
    }

    @GetMapping("send-document-message")
    ResponseEntity<SendMessageResponse> sendDocumentMessage() {
        return ResponseEntity.ok(getSession()
                .sendTemplateMessage("mybillone_stmt_document_1", this.testPhoneNo, "https://filebin.net/j0yb2oqzvqdwx60i/sample.pdf")
        );
    }

    @GetMapping("send-placeholder-message")
    ResponseEntity<SendMessageResponse> sendPlaceholderMessage() {
        return ResponseEntity.ok(getSession().sendTemplateMessage(
                "mybillone_stmt_placeholder_2",
                WhatsAppConstants.LANGUAGE_EN,
                this.testPhoneNo,
                "https://filebin.net/j0yb2oqzvqdwx60i/sample.pdf",
                List.of(
                        "Letrik BILL",
                        "012-111"
                )
        ));
    }

    @GetMapping("send-reply-message")
    ResponseEntity<SendMessageResponse> sendReplyMessage() {
        return ResponseEntity.ok(getSession().sendTextMessage(this.testPhoneNo, "Thanks for your reply!"));
    }

    @GetMapping("register-webhook")
    ResponseEntity<ThirdPartyWebhookResponse> registerWebhook() {
        return ResponseEntity.ok(getSession().registerWebHook("bob", "https://api.uat.grabbill.com/wa-sandbox/hook?clientId=myBillOne"));
    }

    @GetMapping("get-webhooks")
    ResponseEntity<List<ThirdPartyWebhookResponse>> getWebhooks() {
        return ResponseEntity.ok(getSession().getWebhooks());
    }

    @GetMapping("unregister-webhook/{webhookId}")
    ResponseEntity<Boolean> unregisterWebhook(@PathVariable String webhookId) {
        return ResponseEntity.ok(getSession().unregisterWebHook(webhookId));
    }

    @PostMapping("hook")
    ResponseEntity<String> hook(@RequestBody String body) {
        log.info("--- hooked");
        log.info(body);
        return ResponseEntity.ok("ok");
    }

    /**
     * TEST USE ONLY: in production, this session should NOT be cached in-memory within controller
     */
    private WhatsAppSession getSession() {
        if (session == null) {
            session = service.login(wabaGuid, email, password);
        }
        return session;
    }
}
