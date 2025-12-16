package com.grabbill.engine.service.sms;

import com.grabbill.core.utils.SmsUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author michaellow
 */
public class SmsServiceImpl implements SmsService {

    private RestTemplate restTemplate;
    private SmsProperties smsProperties;


    public SmsServiceImpl(
            final SmsProperties smsProperties,
            final RestTemplate restTemplate
    ) {
        this.smsProperties = smsProperties;
        this.restTemplate = restTemplate;
    }

    @Override
    public SmsResponse send(
            final String from,
            final String to,
            final String message
    ) {
        String responseString = null;

        // test-mode, just generate random response
        if (smsProperties.isTestMode()) {
            responseString = randomResponse();

        } else {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("gw-username", smsProperties.getUsername());
            map.add("gw-password", smsProperties.getPassword());
            map.add("gw-from", from);
            map.add("gw-to", to);

            if (SmsUtils.isAsciiTextOnly(message)) {
                map.add("gw-text", message);
            } else {
                map.add("gw-text", SmsUtils.toHex(message));
                map.add("gw-coding", "3");
            }

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    smsProperties.getUrl(),
                    request,
                    String.class
            );
            responseString = response.getBody();
        }

        Map<String, String> responseFieldMap = new HashMap<>();
        String[] responseFields = Objects.requireNonNull(responseString).split("&");
        for (String responseField : responseFields) {
            String[] pair = responseField.split("=");
            responseFieldMap.put(pair[0], pair[1]);
        }

        String status = responseFieldMap.get("status");
        SmsResponseStatusCode statusCode = SmsResponseStatusCode.from(status);
        SmsResponse outResponse = new SmsResponse();
        if (statusCode != null) {
            outResponse.setRaw(responseString);
            outResponse.setStatusCode(statusCode);

            if (SmsResponseStatusCode.OK.equals(statusCode)) {
                outResponse.setMessageId(responseFieldMap.get("msgid"));
                if (responseFieldMap.containsKey("sms_split")) {
                    outResponse.setSmsSplit(Integer.parseInt(responseFieldMap.get("sms_split")));
                } else {
                    outResponse.setSmsSplit(1);
                }

            } else {
                outResponse.setErrorMessage(responseFieldMap.get("err_msg"));
            }
        }
        return outResponse;
    }

    private static final List<Integer> SMS_SPLITS = new ArrayList<>(Arrays.asList(1, 2, 3, 4));

    private String randomResponse() {
        SmsResponseStatusCode randomStatusTemp =
                SmsResponseStatusCode.values()[ThreadLocalRandom.current().nextInt(SmsResponseStatusCode.values().length)];
        SmsResponseStatusCode randomStatus = new ArrayList<>(Arrays.asList(randomStatusTemp, SmsResponseStatusCode.OK)).get(ThreadLocalRandom.current().nextInt(2));

        String response = "status=" + randomStatus.getCode() + "&";
        if (SmsResponseStatusCode.OK.equals(randomStatus)) {
            response += "msgid=cust20013050311050614001";
            int randomSmsSplit = SMS_SPLITS.get(ThreadLocalRandom.current().nextInt(SMS_SPLITS.size()));
            response += "&sms_split=" + randomSmsSplit;

        } else {
            response += "err_msg=" + randomStatus.getDescription();
        }
        return response;
    }

}
