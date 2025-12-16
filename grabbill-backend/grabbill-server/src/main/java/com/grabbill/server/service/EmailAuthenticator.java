package com.grabbill.server.service;

/**
 * @author michaellow
 */
public interface EmailAuthenticator {

    int generateOTP(String email);

    boolean authorize(String email, int otp);

}
