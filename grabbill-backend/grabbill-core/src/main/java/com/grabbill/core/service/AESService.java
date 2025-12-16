package com.grabbill.core.service;

/**
 * @author michaellow
 */
public interface AESService {

    String AES = "AES";
    String AES_ALGORITHM = "AES/CBC/PKCS5Padding";

    String encrypt(
            String input,
            String key,
            String iv
    ) throws Exception;

    String decrypt(
            String cipherText,
            String key,
            String iv
    ) throws Exception;

}
