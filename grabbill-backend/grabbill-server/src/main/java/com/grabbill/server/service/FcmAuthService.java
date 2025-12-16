package com.grabbill.server.service;

import com.grabbill.server.dto.FcmIdTokenPayload;

/**
 * @author michaellow
 */
public interface FcmAuthService {

    FcmIdTokenPayload authenticate(String idToken);

    void revoke(FcmIdTokenPayload fcmIdTokenPayload);

}
