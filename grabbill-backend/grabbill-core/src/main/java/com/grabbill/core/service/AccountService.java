package com.grabbill.core.service;

import com.grabbill.core.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author michaellow
 */
public interface AccountService {

    Optional<Account> getById(Integer id);

    Optional<Account> getByStripeCustomerId(String customerId);

    Account save(Account account);

    Page<Account> getAll(Pageable pageable);

    Page<Account> getAllByCompanyName(String companyName, Pageable pageable);

    Page<Account> searchAccount(String companyName, String planName, OffsetDateTime startCreatedDate, OffsetDateTime endCreatedDate, Pageable pageable);

    List<Account> getByIdNotIn(Set<Integer> ids);

    List<Account> getAccountsBetween(
            final OffsetDateTime startDate,
            final OffsetDateTime endDate,
            final String affiliateMasterCode
    );

    List<Account> getByAffiliateMasterCode(String affiliateMasterCode);

    List<Account> getByAffiliateMasterCodeAndAffiliateSubCode(String masterCode, String subCode);

    List<Account> getByCompanyName(String companyName);

    List<Account> getByCompanyNameAndAffiliateMasterCode(String companyName, String masterCode);

}
