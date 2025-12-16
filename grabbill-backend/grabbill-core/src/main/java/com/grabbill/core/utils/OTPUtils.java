package com.grabbill.core.utils;

import java.util.Random;

/**
 * @author michaellow
 */
public final class OTPUtils {

    private static final String NUMBERS = "0123456789";

    public static int generateOTP(final int length) {
        Random random = new Random();
        char[] otp = new char[length];

        for (int i = 0; i < length; i++) {
            otp[i] = NUMBERS.charAt(random.nextInt(NUMBERS.length()));
        }
        return Integer.parseInt(new String(otp));
    }

}
