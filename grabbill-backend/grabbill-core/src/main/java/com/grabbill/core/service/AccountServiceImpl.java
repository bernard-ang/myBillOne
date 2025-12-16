package com.grabbill.core.service;

import com.grabbill.core.entity.Account;
import com.grabbill.core.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author michaellow
 */
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountRepository accountRepository;


    @Override
    public Optional<Account> getById(final Integer id) {
        return accountRepository.findById(id);
    }

    @Override
    public Optional<Account> getByStripeCustomerId(final String customerId) {
        return accountRepository.findByStripeCustomerIdIs(customerId);
    }

    @Override
    public Account save(final Account account) {
        return accountRepository.save(account);
    }

    @Override
    public Page<Account> getAll(final Pageable pageable) {
        return accountRepository.findAll(pageable);
    }

    @Override
    public Page<Account> getAllByCompanyName(final String companyName, final Pageable pageable) {
        return accountRepository.findAllByCompanyNameContainingIgnoreCase(companyName, pageable);
    }

    @Override
    public Page<Account> searchAccount(final String companyName, final String planName, final OffsetDateTime startCreatedDate, final OffsetDateTime endCreatedDate, final Pageable pageable) {
        return accountRepository.searchAccount(companyName, planName, startCreatedDate, endCreatedDate, pageable);
    }

    @Override
    public List<Account> getByIdNotIn(final Set<Integer> ids) {
        return accountRepository.findByIdNotIn(ids);
    }

    @Override
    public List<Account> getAccountsBetween(
            final OffsetDateTime startDate,
            final OffsetDateTime endDate,
            final String affiliateMasterCode
    ) {
        if (startDate != null && endDate != null) {
            if (StringUtils.hasLength(affiliateMasterCode)) {
                return accountRepository.findByCreatedDateIsBetweenAndAffiliateMasterCodeIs(startDate, endDate, affiliateMasterCode);

            } else {
                return accountRepository.findByCreatedDateIsBetween(startDate, endDate);
            }

        } else if (startDate != null) {
            if (StringUtils.hasLength(affiliateMasterCode)) {
                return accountRepository.findByCreatedDateIsAfterAndAffiliateMasterCodeIs(startDate, affiliateMasterCode);
            } else {
                return accountRepository.findByCreatedDateIsAfter(startDate);
            }

        } else if (endDate != null) {
            if (StringUtils.hasLength(affiliateMasterCode)) {
                return accountRepository.findByCreatedDateIsBeforeAndAffiliateMasterCodeIs(endDate, affiliateMasterCode);

            } else {
                return accountRepository.findByCreatedDateIsBefore(endDate);
            }
        }

        return accountRepository.findAll();
    }

    @Override
    public List<Account> getByAffiliateMasterCode(final String affiliateMasterCode) {
        return accountRepository.findByAffiliateMasterCodeIs(affiliateMasterCode);
    }

    @Override
    public List<Account> getByAffiliateMasterCodeAndAffiliateSubCode(final String masterCode, final String subCode) {
        return accountRepository.findByAffiliateMasterCodeIsAndAffiliateSubCodeIs(masterCode, subCode);
    }

    @Override
    public List<Account> getByCompanyName(final String companyName) {
        return accountRepository.findByCompanyNameIs(companyName);
    }

    @Override
    public List<Account> getByCompanyNameAndAffiliateMasterCode(final String companyName, final String masterCode) {
        return accountRepository.findByCompanyNameIsAndAffiliateMasterCodeIs(companyName, masterCode);
    }

}
