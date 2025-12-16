package com.grabbill.server.service;

import com.grabbill.core.entity.User;

/**
 * @author michaellow
 */
public interface UserRegistrationService {

    User register(String name, String email, String password, String affiliateCode);

    User registerByFcm(String fcmUid, String name, String email, String password);

}
