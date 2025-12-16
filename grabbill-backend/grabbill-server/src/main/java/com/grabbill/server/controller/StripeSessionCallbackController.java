package com.grabbill.server.controller;

import com.grabbill.core.entity.Account;
import com.grabbill.core.service.AccountService;
import com.grabbill.server.GrabbillServerErrorCode;
import com.grabbill.server.exception.GrabbillServerException;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.SetupIntent;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerUpdateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Map;

/**
 * @author michaellow
 */
@Slf4j
@RestController
@RequestMapping("/callback/stripe/sessions")
public class StripeSessionCallbackController {

    private static final String COMPLETE = "complete";
    private static final String SUCCEEDED = "succeeded";

    @Value("${payment.stripe.session.billing-portal.setup.return.url}")
    private String setupReturnUrl;

    @Autowired
    private AccountService accountService;


    @GetMapping
    public ResponseEntity<String> handleSession(@RequestParam String sessionId) {
        StringBuilder queryParams = new StringBuilder();
        try {
            Session session = Session.retrieve(sessionId);
            SetupIntent setupIntent = SetupIntent.retrieve(session.getSetupIntent());
            Account account = accountService.getByStripeCustomerId(session.getCustomer()).orElseThrow(() ->
                    new GrabbillServerException(GrabbillServerErrorCode.GRB0001, "Unable to process session - " + sessionId)
            );
            Map<String, String> metadata = session.getMetadata();
            for (Map.Entry<String, String> metadataEntry : metadata.entrySet()) {
                queryParams.append("&").append(metadataEntry.getKey()).append("=").append(metadataEntry.getValue());
            }

            if (account != null
                    && COMPLETE.equalsIgnoreCase(session.getStatus())
                    && SUCCEEDED.equalsIgnoreCase(setupIntent.getStatus())) {

                // attach payment method to customer and set as default payment method
                Customer.retrieve(setupIntent.getCustomer())
                        .update(CustomerUpdateParams.builder()
                                .setInvoiceSettings(
                                        CustomerUpdateParams.InvoiceSettings.builder()
                                                .setDefaultPaymentMethod(setupIntent.getPaymentMethod())
                                                .build()
                                )

                                .setPhone(StringUtils.hasLength(account.getCompanyContactNo()) ?
                                        account.getCompanyContactNo() : "")
                                .build()
                        );
            }
        } catch (StripeException e) {
            log.error("Unable to process session - " + sessionId, e);
            throw new GrabbillServerException(GrabbillServerErrorCode.GRB0001, "Unable to process session - " + sessionId);
        }

        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(setupReturnUrl + queryParams)).build();
    }

}
