package com.grabbill.server.security;

/**
 * @author michaellow
 */
public interface TokenCryptoServices {

    String encrypt(String value);

    String decrypt(String value);

}
