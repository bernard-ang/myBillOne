package com.grabbill.server.controller;

import com.grabbill.core.entity.User;
import com.grabbill.server.GrabbillServerErrorCode;
import com.grabbill.server.exception.GrabbillServerException;
import com.grabbill.server.service.AccountPaymentCheckService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author michaellow
 */
public class PaymentAwareController {

    @Autowired
    AccountPaymentCheckService accountPaymentCheckService;


    void verifyPaymentStatus(final User user) {
        if (accountPaymentCheckService.isPaymentGracePeriodOver(user.getAccount())) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB8005,
                    "User account with unpaid invoice exceeded payment grace period"
            );
        }
    }

}
