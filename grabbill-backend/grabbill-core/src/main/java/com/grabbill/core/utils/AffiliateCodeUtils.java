package com.grabbill.core.utils;

import org.springframework.util.StringUtils;

/**
 * @author michaellow
 */
public final class AffiliateCodeUtils {

    public static final String[] ALPHANUMERIC = {
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
            "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
            "U", "V", "W", "X", "Y", "Z"
    };

    public static String toAffiliateCode(final String masterCode, final String subCode) {
        String affiliateCode = null;
        if (StringUtils.hasLength(masterCode) && StringUtils.hasLength(subCode)) {
            affiliateCode = masterCode + "-" + subCode;

        } else if (StringUtils.hasLength(masterCode)) {
            affiliateCode = masterCode;
        }

        return affiliateCode;
    }

}
