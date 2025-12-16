package com.grabbill.core.service;

import com.grabbill.core.entity.*;

import java.util.Optional;

/**
 * @author michaellow
 */
public interface UnsubscribedEmailLinkService {

    Optional<UnsubscribedEmailLink> getByLinkId(String linkId);

    UnsubscribedEmailLink generate(
            Account account,
            BaseType targetType,
            BaseActivity targetActivity,
            String email
    );

    UnsubscribedEmailLink save(UnsubscribedEmailLink target);

}
