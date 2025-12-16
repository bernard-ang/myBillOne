package com.grabbill.core.utils;

import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * @author michaellow
 */
public class SmsUtils {

    private static final Pattern MY_PHONE_PATTERN = Pattern.compile("^(\\+?6?01)[02-46-9]-*[0-9]{7}$|^(\\+?6?01)[1]-*[0-9]{8}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern INTERNATIONAL_PHONE_PATTERN = Pattern.compile("\\+?(9[976]\\d|8[987530]\\d|6[987]\\d|5[90]\\d|42\\d|3[875]\\d|2[98654321]\\d|9[8543210]|8[6421]|6[6543210]|5[87654321]|4[987654310]|3[9643210]|2[70]|7|1)\\d{1,14}$", Pattern.CASE_INSENSITIVE);
    private static final String ASCII_PATTERN = "\\A\\p{ASCII}*\\z";
    private static final String RM0 = "RM0 ";
    private static final int MAX_LENGTH = 140;


    public static boolean isAsciiTextOnly(final String value) {
        return value.matches(ASCII_PATTERN);
    }

    public static int countBytes(final String value) throws UnsupportedEncodingException {
        if (isAsciiTextOnly(value)) {
            return value.getBytes(StandardCharsets.UTF_8).length;
        }
        return value.getBytes(StandardCharsets.UTF_16).length;
    }

    public static int estimateCreditUsage(final String value) throws UnsupportedEncodingException {
        int bytes = countBytes(value);
        int credits;
        if (bytes > MAX_LENGTH) {
            credits = bytes / MAX_LENGTH;
            if ((bytes % MAX_LENGTH) > 0) {
                credits++;
            }

            return credits;
        }
        return 1;
    }

    public static String toHex(final String value) {
        StringBuilder stringBuffer = new StringBuilder();
        char[] chars = value.toCharArray();
        for (char c : chars) {
            String hexString = Integer.toHexString(c);
            if (hexString.length() == 2) {
                stringBuffer.append("00");
            }
            stringBuffer.append(hexString);
        }
        return stringBuffer.toString();
    }

    public static boolean isValidPhoneNumber(final String value) {
        if (!StringUtils.hasLength(value)) {
            return false;
        }
        return (MY_PHONE_PATTERN.matcher(value).find());
    }

    public static boolean isValidWhatsAppPhoneNumber(final String value) {
        if (!StringUtils.hasLength(value)) {
            return false;
        }
        return (INTERNATIONAL_PHONE_PATTERN.matcher(value).find());
    }

    public static String appendRM0(final String value) {
        if (!value.startsWith(RM0)) {
            return RM0 + value;
        }
        return value;
    }

}
