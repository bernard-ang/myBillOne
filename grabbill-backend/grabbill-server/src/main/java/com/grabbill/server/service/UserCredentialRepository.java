package com.grabbill.server.service;

import com.grabbill.core.entity.User;
import com.grabbill.core.service.UserService;
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
public class UserCredentialRepository implements ICredentialRepository {

    @Autowired
    private UserService userService;


    @Override
    public String getSecretKey(final String username) {
        User target = getUser(username);
        return target.getGoogle2FASecretKey();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void saveUserCredentials(
            final String username,
            final String secretKey,
            final int validationCode,
            final List<Integer> scratchCodes
    ) {
        User target = getUser(username);
        target.setGoogle2FASecretKey(secretKey);
        target.setGoogle2FAValidationCode(validationCode);

        userService.save(target);
    }

    private User getUser(final String username) {
        return userService.getByEmail(username).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB1002,
                        "User [" + username + "] is not found!"
                )
        );
    }

}

