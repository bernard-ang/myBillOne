package com.grabbill.core.service.whatsapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.grabbill.core.model.whatsapp.WhatsAppResponse;
import com.grabbill.core.model.whatsapp.response.AuthenticateBlastResponse;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

@Slf4j
public class WhatsAppService {
    public static final String API_BASE_URL = "https://nubitel.app/";

    public static final String API_LOGIN = "AuthenticateBlast?email=%s&password=%s";
    public static final String API_TEMPLATE_REFRESH = "api/WhatsappTemplate/RefereshWhatappTemplate/%s";
    public static final String API_TEMPLATE_SEND_MESSAGE = "api/WhatsappTemplate/SendMessageTemplate";
    public static final String API_TEMPLATE = "api/WhatsappTemplate";
    public static final String API_WEBHOOK = "api/ThirdPartyWebhook";
    public static final String API_SEND_TEXT_MESSAGE = "api/WhatsappMessage/SendTextMessage";
    public static final String API_SEND_IMAGE_MESSAGE_BY_URL = "api/WhatsappMessage/SendImageMessageByURL";
    public static final String API_SEND_DOCUMENT_MESSAGE_BY_URL = "api/WhatsappMessage/SendDocumentMessageByURL";

    private final RestTemplate restTemplate;

    @Getter
    private final ObjectMapper mapper;

    public WhatsAppService(ObjectMapper mapper) {
        this.mapper = mapper;
        this.restTemplate = new RestTemplateBuilder().build();
    }

    public WhatsAppSession login(String wabaGuid, String email, String password) {
        // perform login
        try {
            ResponseEntity<WhatsAppResponse<AuthenticateBlastResponse>> response = restTemplate.exchange(
                    makeApiUri(format(API_LOGIN, email, password)),
                    HttpMethod.POST,
                    makeHttpEntity(),
                    new ParameterizedTypeReference<>() {
                    });

            AuthenticateBlastResponse responseData = requireNonNull(response.getBody()).getData();
            checkState(!responseData.getToken().isEmpty(), "Token must not be empty");
            return new WhatsAppSession(this, wabaGuid, responseData);
        } catch (HttpClientErrorException exception) {
            Gson gson = new Gson();
            WhatsAppResponse whatsAppResponse = gson.fromJson(exception.getResponseBodyAsString(), WhatsAppResponse.class);

            throw new WhatsAppServiceException(
                    WhatsAppErrorCode.GRB1501,
                    whatsAppResponse.getMessage(),
                    exception
            );
        }
    }

    public <T> HttpEntity<T> makeHttpEntity(T body) {
        return new HttpEntity<T>(body, makeHeaders());
    }

    public <T> HttpEntity<T> makeHttpEntity() {
        return new HttpEntity<T>(makeHeaders());
    }

    public HttpHeaders makeHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, "application/json, text/plain");
        return headers;
    }

    public URI makeApiUri(String api) {
        try {
            return new URI(API_BASE_URL + api);
        } catch (URISyntaxException e) {
            throw new WhatsAppServiceException("Error making API URI", e);
        }
    }
}
