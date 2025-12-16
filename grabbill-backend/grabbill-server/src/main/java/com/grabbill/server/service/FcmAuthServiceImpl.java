package com.grabbill.server.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.grabbill.core.exception.GrabbillException;
import com.grabbill.server.GrabbillServerErrorCode;
import com.grabbill.server.dto.FcmIdTokenPayload;
import com.grabbill.server.exception.GrabbillServerException;

/**
 * @author michaellow
 */
public class FcmAuthServiceImpl implements FcmAuthService {

    @Override
    public FcmIdTokenPayload authenticate(final String idToken) {

        // verify the authenticated tokenId, decode token for uid / email / name
        FirebaseToken decodedToken;
        try {
            decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
        } catch (FirebaseAuthException e) {
            throw new GrabbillServerException(GrabbillServerErrorCode.GRB0102, "Token authorization failed", e);
        }

        // TODO: confirm google authenticated token is with email verified
//        if (!decodedToken.isEmailVerified()) {
//            throw new GrabbillServerException(GrabbillServerErrorCode.GRB0102, "Email is not verified");
//        }

        FcmIdTokenPayload payload = new FcmIdTokenPayload();
        payload.setUid(decodedToken.getUid());
        payload.setEmail(decodedToken.getEmail());
        payload.setName(decodedToken.getName());

        return payload;
    }

    @Override
    public void revoke(final FcmIdTokenPayload fcmIdTokenPayload) {
        try {
            FirebaseAuth.getInstance().revokeRefreshTokens(fcmIdTokenPayload.getUid());
        } catch (FirebaseAuthException e) {
            throw new GrabbillServerException(GrabbillServerErrorCode.GRB0103, "Token revoke failed", e);
        }
    }
}
