package com.grabbill.server.security;

import com.grabbill.core.exception.GrabbillException;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

/**
 * @author michaellow
 */
public class TokenCryptoServicesImpl implements TokenCryptoServices {

    @Value("${security.crypto.token.key:q9]x@Yh2\">{,=HKx}")
    private String cryptoTokenKey;

    private SecretKeySpec secretKey;


    @Override
    public String encrypt(final String value) {
        if (secretKey == null) {
            initialize();
        }

        if (value != null) {
            try {
                Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                return Base64.getEncoder().encodeToString(cipher.doFinal(value.getBytes(StandardCharsets.UTF_8)));

            } catch (Exception e) {
                throw new GrabbillException("Token encryption failed!", e);
            }
        }

        return null;
    }

    @Override
    public String decrypt(final String value) {
        if (secretKey == null) {
            initialize();
        }

        if (value != null) {
            try {
                Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                cipher.init(Cipher.DECRYPT_MODE, secretKey);
                return new String(cipher.doFinal(Base64.getDecoder().decode(value)));

            } catch (Exception e) {
                throw new GrabbillException("Token decryption failed!", e);
            }
        }

        return null;
    }

    private void initialize() {
        try {
            byte[] key = cryptoTokenKey.getBytes(StandardCharsets.UTF_8);
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            secretKey = new SecretKeySpec(key, "AES");

        } catch (NoSuchAlgorithmException e) {
            throw new GrabbillException(e);
        }
    }

}
