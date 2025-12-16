package com.grabbill.core.service;

import com.grabbill.core.entity.Account;
import com.grabbill.core.entity.MailServer;
import com.grabbill.core.repository.MailServerRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

/**
 * @author michaellow
 */
public class MailServerServiceImpl implements MailServerService {

    @Autowired
    private MailServerRepository repository;


    @Override
    public Optional<MailServer> getByAccount(final Account account) {
        return repository.findByAccount(account);
    }

    @Override
    public Optional<MailServer> getByAccountId(final Integer accountId) {
        return repository.findByAccountId(accountId);
    }

    @Override
    public MailServer save(final MailServer mailServer) {
        return repository.save(mailServer);
    }

}
