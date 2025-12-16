package com.grabbill.core.service;

import com.grabbill.core.entity.*;
import com.grabbill.core.model.DomainType;
import com.grabbill.core.repository.UnsubscribedEmailLinkRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

/**
 * @author michaellow
 */
public class UnsubscribedEmailLinkServiceImpl implements UnsubscribedEmailLinkService {

    private static final int UUID_LENGTH = 150;

    @Autowired
    private UnsubscribedEmailLinkRepository repository;


    @Override
    public Optional<UnsubscribedEmailLink> getByLinkId(final String linkId) {
        return repository.findByLinkIdIs(linkId);
    }

    @Override
    public UnsubscribedEmailLink generate(
            final Account account,
            final BaseType targetType,
            final BaseActivity targetActivity,
            final String email
    ) {
        String randomId = RandomStringUtils.randomAlphanumeric(UUID_LENGTH);
        while (repository.findByLinkIdIs(randomId).isPresent()) {
            randomId = RandomStringUtils.randomAlphanumeric(UUID_LENGTH);
        }

        UnsubscribedEmailLink instance = new UnsubscribedEmailLink();
        instance.setLinkId(randomId);

        if (targetType instanceof TransactionalEmailType) {
            instance.setDomainType(DomainType.TRANSACTIONAL_EMAIL);
            instance.setTypeId(((TransactionalEmailType) targetType).getId());
            instance.setActivityId(((TransactionalEmailActivity) targetActivity).getId());

        } else if (targetType instanceof MTTransactionalEmailType) {
            instance.setDomainType(DomainType.MT_TRANSACTIONAL_EMAIL);
            instance.setTypeId(((MTTransactionalEmailType) targetType).getId());
            instance.setActivityId(((MTTransactionalEmailActivity) targetActivity).getId());

        } else if (targetType instanceof EmailCampaignType) {
            instance.setDomainType(DomainType.EMAIL_CAMPAIGN);
            instance.setTypeId(((EmailCampaignType) targetType).getId());
            instance.setActivityId(((EmailCampaignActivity) targetActivity).getId());
        }
        instance.setEmail(email);

        return repository.save(instance);
    }

    @Override
    public UnsubscribedEmailLink save(final UnsubscribedEmailLink target) {
        return repository.save(target);
    }

}
