package com.grabbill.core.service.whatsapp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.grabbill.core.LoggingInterceptor;
import com.grabbill.core.model.whatsapp.request.*;
import com.grabbill.core.model.whatsapp.WhatsAppConstants;
import com.grabbill.core.model.whatsapp.WhatsAppResponse;
import com.grabbill.core.model.whatsapp.response.AuthenticateBlastResponse;
import com.grabbill.core.model.whatsapp.response.RefreshTemplateResponse;
import com.grabbill.core.model.whatsapp.response.SendMessageResponse;
import com.grabbill.core.model.whatsapp.response.ThirdPartyWebhookResponse;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import static com.grabbill.core.service.whatsapp.WhatsAppService.*;
import static java.lang.String.format;
import static java.util.List.of;
import static java.util.Objects.requireNonNull;

@Slf4j
public class WhatsAppSession {
    private WhatsAppService service;
    private String wabaGuid;

    @Getter
    private AuthenticateBlastResponse authInfo;

    private RestTemplate restTemplate;

    public WhatsAppSession(WhatsAppService service, String wabaGuid, AuthenticateBlastResponse authInfo) {
        this.service = service;
        this.wabaGuid = wabaGuid;
        this.authInfo = authInfo;

        this.restTemplate = new RestTemplateBuilder(rt -> rt.getInterceptors().add((request, body, execution) -> {
            if (authInfo != null) {
                request.getHeaders().add(HttpHeaders.AUTHORIZATION, "Bearer " + authInfo.getToken());
            }
            return execution.execute(request, body);
        })).build();
        this.restTemplate.getInterceptors().add(new LoggingInterceptor());
    }

    public ThirdPartyWebhookResponse registerWebHook(String name, String callbackUrl) {
        ensureTokenNotExpired();

        try {
            ThirdPartyWebhook thirdPartyWebhook = ThirdPartyWebhook.builder()
                    .name(name)
                    .callbackURL(callbackUrl)
                    .service(1)
                    .serviceId(this.wabaGuid)
                    .build();

            ResponseEntity<WhatsAppResponse<ThirdPartyWebhookResponse>> response = restTemplate.exchange(
                    service.makeApiUri(API_WEBHOOK),
                    HttpMethod.POST,
                    service.makeHttpEntity(thirdPartyWebhook),
                    new ParameterizedTypeReference<>() {
                    }
            );

            return requireNonNull(response.getBody()).getData();
        } catch (HttpClientErrorException exception) {
            Gson gson = new Gson();
            WhatsAppResponse whatsAppResponse = gson.fromJson(exception.getResponseBodyAsString(), WhatsAppResponse.class);

            throw new WhatsAppServiceException(
                    WhatsAppErrorCode.GRB1503,
                    whatsAppResponse.getMessage(),
                    exception
            );
        }
    }

    public Boolean unregisterWebHook(String webhookId) {
        ensureTokenNotExpired();

        try {
            ResponseEntity<WhatsAppResponse<String>> response = restTemplate.exchange(
                    service.makeApiUri(API_WEBHOOK + "/" + webhookId),
                    HttpMethod.DELETE,
                    service.makeHttpEntity(),
                    new ParameterizedTypeReference<>() {
                    }
            );

            return requireNonNull(response.getBody()).isStatus();
        } catch (HttpClientErrorException exception) {
            Gson gson = new Gson();
            WhatsAppResponse whatsAppResponse = gson.fromJson(exception.getResponseBodyAsString(), WhatsAppResponse.class);

            throw new WhatsAppServiceException(
                    WhatsAppErrorCode.GRB1502,
                    whatsAppResponse.getMessage(),
                    exception
            );
        }
    }

    public List<ThirdPartyWebhookResponse> getWebhooks() {
        ensureTokenNotExpired();

        ResponseEntity<WhatsAppResponse<ThirdPartyWebhookResponse[]>> exchange = restTemplate.exchange(
                service.makeApiUri(API_WEBHOOK),
                HttpMethod.GET,
                service.makeHttpEntity(),
                new ParameterizedTypeReference<>() {
                }
        );

        return List.of(requireNonNull(exchange.getBody()).getData());
    }

    public List<RefreshTemplateResponse> refreshTemplate() {
        ensureTokenNotExpired();

        try {
            ResponseEntity<WhatsAppResponse<RefreshTemplateResponse[]>> exchange = restTemplate.exchange(
                    service.makeApiUri(format(API_TEMPLATE_REFRESH, wabaGuid)),
                    HttpMethod.GET,
                    service.makeHttpEntity(),
                    new ParameterizedTypeReference<>() {
                    }
            );

            return List.of(requireNonNull(exchange.getBody()).getData());
        } catch (HttpClientErrorException exception) {
            Gson gson = new Gson();
            WhatsAppResponse whatsAppResponse = gson.fromJson(exception.getResponseBodyAsString(), WhatsAppResponse.class);

            throw new WhatsAppServiceException(
                    WhatsAppErrorCode.GRB1504,
                    whatsAppResponse.getMessage(),
                    exception
            );
        }
    }

    public RefreshTemplateResponse createTemplate(TemplateRequest templateRequest) {
        templateRequest.setWhatsappId(wabaGuid);

        try {
            Gson gson = new Gson();
            System.out.println(gson.toJson(templateRequest));

            ResponseEntity<WhatsAppResponse<RefreshTemplateResponse>> response = restTemplate.exchange(
                    service.makeApiUri(API_TEMPLATE),
                    HttpMethod.POST,
                    service.makeHttpEntity(templateRequest),
                    new ParameterizedTypeReference<>() {
                    }
            );

            System.out.println(response.getStatusCode());
            System.out.println(response.getStatusCodeValue());
            System.out.println(response.getBody());

            return requireNonNull(response.getBody()).getData();
        } catch (HttpClientErrorException exception) {
            log.error("Error creating WhatsApp template during API call", exception);

            Gson gson = new Gson();
            WhatsAppResponse whatsAppResponse = gson.fromJson(exception.getResponseBodyAsString(), WhatsAppResponse.class);

            throw new WhatsAppServiceException(
                    WhatsAppErrorCode.GRB1505,
                    Optional.ofNullable(whatsAppResponse.getMessage()).orElse("Error creating WhatsApp template"),
                    exception
            );
        }
    }

    /**
     * @param templateId based on the "id" returned by refreshTemplate()
     */
    public String deleteTemplate(String templateId) {
        try {
            ResponseEntity<WhatsAppResponse<String>> response = restTemplate.exchange(
                    service.makeApiUri(API_TEMPLATE + "/" + templateId),
                    HttpMethod.DELETE, service.makeHttpEntity(), new ParameterizedTypeReference<>() {
                    }
            );

            System.out.println(response.getStatusCode());
            System.out.println(response.getStatusCodeValue());
            System.out.println(response.getBody());

            return requireNonNull(response.getBody()).getData();
        } catch (HttpClientErrorException exception) {
            Gson gson = new Gson();
            WhatsAppResponse whatsAppResponse = gson.fromJson(exception.getResponseBodyAsString(), WhatsAppResponse.class);

            throw new WhatsAppServiceException(
                    WhatsAppErrorCode.GRB1506,
                    whatsAppResponse.getMessage(),
                    exception
            );
        }
    }

    public SendMessageResponse sendTemplateMessage(String templateName, String to) {
        return this.sendTemplateMessage(templateName, WhatsAppConstants.LANGUAGE_EN, to, null, null);
    }

    public SendMessageResponse sendTemplateMessage(String templateName, String to, @Nullable String documentUrl) {
        return this.sendTemplateMessage(templateName, WhatsAppConstants.LANGUAGE_EN, to, documentUrl, null);
    }

    public SendMessageResponse sendTemplateMessage(String templateName, String templateLanguage, String to, @Nullable String documentUrl, @Nullable List<String> parameters) {
        return this.sendTemplateMessage(templateName, templateLanguage, to, documentUrl, parameters, null);
    }

    public SendMessageResponse sendTemplateMessage(String templateName, String templateLanguage, String to, @Nullable String documentUrl, @Nullable List<String> parameters, @Nullable List<String> ackParameters) {
        List<SendMessageTemplateRequestComponent> components = new ArrayList<>();

        // --- PDF always in HEADER component
        if (documentUrl != null) {
            components.add(SendMessageTemplateRequestComponent.builder()
                    .type("HEADER")
                    .parameters(of(SendMessageTemplateRequestComponentParameter.builder()
                            .type("DOCUMENT")
                            .value(documentUrl)
                            .build()
                    ))
                    .build());
        }

        // --- only support placeholders in BODY component
        // --- only support text placeholders
        List<SendMessageTemplateRequestComponentParameter> placeholderParameters = Optional
                .ofNullable(parameters)
                .orElse(Collections.emptyList())
                .stream()
                .map(value -> SendMessageTemplateRequestComponentParameter.builder()
                        .type("text")
                        .value(value)
                        .build())
                .collect(Collectors.toList());

        if (!placeholderParameters.isEmpty()) {
            components.add(SendMessageTemplateRequestComponent.builder()
                    .type("BODY")
                    .parameters(placeholderParameters)
                    .build());
        }

        // --- Ack button component
        if (ackParameters != null) {
            List<SendMessageTemplateRequestComponentParameter> ackPlaceholderParameters = Optional
                    .of(ackParameters)
                    .orElse(Collections.emptyList())
                    .stream()
                    .map(value -> SendMessageTemplateRequestComponentParameter.builder()
                            .type("text")
                            .value(value)
                            .build())
                    .collect(Collectors.toList());

            components.add(SendMessageTemplateRequestComponent.builder()
                    .type("BUTTON")
                    .sub_type("URL")
                    .index("0")
                    .parameters(ackPlaceholderParameters)
                    .build());
        }

        SendMessageTemplateRequest request = SendMessageTemplateRequest.builder()
                .to(to)
                .name(templateName)
                .component(components)
                .language(SendMessageTemplateRequestLanguage.builder()
                        .code(templateLanguage).build())
                .build();

        return this.sendTemplateMessage(request);
    }

    public SendMessageResponse sendTemplateMessage(SendMessageTemplateRequest request) {
        ensureTokenNotExpired();

        request.setWhatsappId(this.wabaGuid);

        ResponseEntity<WhatsAppResponse<String>> response = restTemplate.exchange(
                service.makeApiUri(API_TEMPLATE_SEND_MESSAGE),
                HttpMethod.POST,
                service.makeHttpEntity(request),
                new ParameterizedTypeReference<>() {
                });

        try {
            String jsonString = requireNonNull(response.getBody()).getData();
            return service.getMapper().readValue(jsonString, SendMessageResponse.class);
        } catch (JsonProcessingException e) {
            throw new WhatsAppSessionException("Error mapping API response", e);
        }
    }

    public SendMessageResponse sendTextMessage(String to, String message) {
        SendTextMessageRequest messageRequest = SendTextMessageRequest.builder()
                .whatsappId(wabaGuid)
                .to(to)
                .message(message)
                .build();

        ResponseEntity<WhatsAppResponse<String>> response = restTemplate.exchange(
                service.makeApiUri(API_SEND_TEXT_MESSAGE),
                HttpMethod.POST,
                service.makeHttpEntity(messageRequest),
                new ParameterizedTypeReference<>() {
                }
        );

        try {
            String jsonString = requireNonNull(response.getBody()).getData();
            return service.getMapper().readValue(jsonString, SendMessageResponse.class);
        } catch (JsonProcessingException e) {
            throw new WhatsAppSessionException("Error mapping API response", e);
        }
    }

    private void ensureTokenNotExpired() {
        Date currentDate = new Date();
        boolean expired = authInfo.getExpiryDate().before(currentDate);

        Preconditions.checkState(!expired, "Auth token has expired");
    }
}
