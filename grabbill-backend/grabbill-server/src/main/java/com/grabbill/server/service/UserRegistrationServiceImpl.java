package com.grabbill.server.service;

import com.grabbill.core.entity.*;
import com.grabbill.core.exception.GrabbillException;
import com.grabbill.core.repository.AccountRepository;
import com.grabbill.core.repository.RoleRepository;
import com.grabbill.core.repository.UserRepository;
import com.grabbill.core.service.AccountUsageStatisticService;
import com.grabbill.core.service.AffiliateCodeService;
import com.grabbill.core.service.ContactFieldService;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

/**
 * @author michaellow
 */
public class UserRegistrationServiceImpl implements UserRegistrationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountUsageStatisticService accountUsageStatisticService;

    @Autowired
    private ContactFieldService contactFieldService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AffiliateCodeService affiliateCodeService;


    @Override
    public User register(
            final String name,
            final String email,
            final String password,
            final String affiliateCode
    ) {
        String masterCode = affiliateCode;
        String subCode = null;

        if (StringUtils.hasLength(affiliateCode)) {
            if (affiliateCode.contains("-")) {
                String[] codes = affiliateCode.split("-");
                masterCode = codes.length > 0 ? codes[0] : affiliateCode;
                subCode = codes.length > 1 ? codes[1] : null;
            }

            // validates if master code exist
            affiliateCodeService.getByCode(masterCode).orElseThrow(
                    () -> new GrabbillException("Invalid affiliate code - " + affiliateCode + "!")
            );
        }

        return registerInternal(
                null,
                name,
                email,
                password,
                masterCode,
                subCode,
                false
        );
    }

    @Override
    public User registerByFcm(
            final String fcmUid,
            final String name,
            final String email,
            final String password
    ) {
        return registerInternal(
                fcmUid,
                name,
                email,
                password,
                null,
                null,
                true
        );
    }

    private User registerInternal(
            final String fcmUid,
            final String name,
            final String email,
            final String password,
            final String affiliateMasterCode,
            final String affiliateSubCode,
            final boolean skipVerification
    ) {
        Account account = new Account();
        account.setAffiliateMasterCode(affiliateMasterCode);
        account.setAffiliateSubCode(affiliateSubCode);
        Account newAccount = accountRepository.save(account);
        contactFieldService.createDefaultContactFields(newAccount);
        accountUsageStatisticService.createNew(newAccount);

        User user = new User();
        user.setFcmUid(fcmUid);
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password != null ? passwordEncoder.encode(password) : null);
        user.setActive(skipVerification);
        user.setVerified(skipVerification);
        user.setAccountActive(true);
        user.setVerificationCode(RandomStringUtils.randomAlphanumeric(15));
        user.setRole(getOwnerRole());
        user.setCreatedBy(name);
        user.setAccount(newAccount);

        return userRepository.save(user);
    }

    private Role getOwnerRole() {
        return roleRepository.findByNameIs("OWNER").orElseThrow(
                () -> new GrabbillException("Configuration error - role [OWNER] not found!")
        );
    }
}
