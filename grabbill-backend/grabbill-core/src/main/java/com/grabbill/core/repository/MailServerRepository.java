package com.grabbill.core.repository;

import com.grabbill.core.entity.Account;
import com.grabbill.core.entity.MailServer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author michaellow
 */
public interface MailServerRepository extends JpaRepository<MailServer, Integer> {

    Optional<MailServer> findByAccount(Account account);

    Optional<MailServer> findByAccountId(Integer accountId);

}
