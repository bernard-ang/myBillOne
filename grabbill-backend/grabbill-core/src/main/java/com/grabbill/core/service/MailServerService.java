package com.grabbill.core.service;

import com.grabbill.core.entity.Account;
import com.grabbill.core.entity.MailServer;

import java.util.Optional;

/**
 * @author michaellow
 */
public interface MailServerService {

    Optional<MailServer> getByAccount(Account account);

    Optional<MailServer> getByAccountId(Integer accountId);

    MailServer save(MailServer mailServer);

}
