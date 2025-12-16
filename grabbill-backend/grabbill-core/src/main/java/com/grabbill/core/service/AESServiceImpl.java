package com.grabbill.core.service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * @author michaellow
 */
public class AESServiceImpl implements AESService {

    @Override
    public String encrypt(
            final String input,
            final String rawKey,
            final String rawIv
    ) throws Exception {
        SecretKey key = generateKey(rawKey);
        IvParameterSpec iv = generateIv(rawIv);
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);

        byte[] cipherText = cipher.doFinal(input.getBytes());
        return Base64.getEncoder().encodeToString(cipherText);
    }

    @Override
    public String decrypt(
            final String cipherText,
            final String rawKey,
            final String rawIv
    ) throws Exception {
        SecretKey key = generateKey(rawKey);
        IvParameterSpec iv = generateIv(rawIv);
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key, iv);

        byte[] plainText = cipher.doFinal(Base64.getDecoder().decode(cipherText));
        return new String(plainText);
    }

    private SecretKey generateKey(final String encryptionKey) {
        byte[] key = Base64.getDecoder().decode(encryptionKey);
        return new SecretKeySpec(key, AES);
    }

    private IvParameterSpec generateIv(final String encryptionIv) {
        byte[] iv = Base64.getDecoder().decode(encryptionIv);
        return new IvParameterSpec(iv);
    }

}
