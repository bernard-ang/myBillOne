package com.grabbill.server.service;

import com.grabbill.core.entity.AdminUser;
import com.grabbill.core.service.AdminUserService;
import com.grabbill.server.GrabbillServerErrorCode;
import com.grabbill.server.exception.GrabbillServerException;
import com.warrenstrange.googleauth.ICredentialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author michaellow
 */
public class AdminUserCredentialRepository implements ICredentialRepository {

    @Autowired
    private AdminUserService adminUserService;


    @Override
    public String getSecretKey(final String adminUsername) {
        AdminUser target = getAdminUser(adminUsername);
        return target.getGoogle2FASecretKey();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void saveUserCredentials(
            final String username,
            final String secretKey,
            final int validationCode,
            final List<Integer> scratchCodes
    ) {
        AdminUser target = getAdminUser(username);
        target.setGoogle2FASecretKey(secretKey);
        target.setGoogle2FAValidationCode(validationCode);

        adminUserService.save(target);
    }

    private AdminUser getAdminUser(final String adminUsername) {
        return adminUserService.getByEmail(adminUsername).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB7001,
                        "Admin user [" + adminUsername + "] is not found!"
                )
        );
    }

}

