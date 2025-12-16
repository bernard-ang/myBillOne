package com.grabbill.core.repository;

import com.grabbill.core.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author michaellow
 */
public interface AccountRepository extends JpaRepository<Account, Integer>, AccountRepositoryCustom {

    Optional<Account> findByStripeCustomerIdIs(String customerId);

    Page<Account> findAllByCompanyNameContainingIgnoreCase(String companyName, Pageable pageable);

    List<Account> findByIdNotIn(Set<Integer> ids);

    List<Account> findByCreatedDateIsBefore(OffsetDateTime dateTime);

    List<Account> findByCreatedDateIsBeforeAndAffiliateMasterCodeIs(OffsetDateTime dateTime, String masterCode);

    List<Account> findByCreatedDateIsAfter(OffsetDateTime dateTime);

    List<Account> findByCreatedDateIsAfterAndAffiliateMasterCodeIs(OffsetDateTime dateTime, String masterCode);

    List<Account> findByCreatedDateIsBetween(OffsetDateTime startDateTime, OffsetDateTime endDateTime);

    List<Account> findByCreatedDateIsBetweenAndAffiliateMasterCodeIs(OffsetDateTime startDateTime, OffsetDateTime endDateTime, String masterCode);

    List<Account> findByAffiliateMasterCodeIs(String masterCode);

    List<Account> findByAffiliateMasterCodeIsAndAffiliateSubCodeIs(String masterCode, String subCode);

    List<Account> findByCompanyNameIs(String companyName);

    List<Account> findByCompanyNameIsAndAffiliateMasterCodeIs(String companyName, String masterCode);

}
