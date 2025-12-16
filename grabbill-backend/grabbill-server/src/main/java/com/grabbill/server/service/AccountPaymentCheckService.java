package com.grabbill.server.service;

import com.grabbill.core.entity.Account;

/**
 * @author michaellow
 */
public interface AccountPaymentCheckService {

    boolean isPaymentGracePeriodOver(Account account);

}
