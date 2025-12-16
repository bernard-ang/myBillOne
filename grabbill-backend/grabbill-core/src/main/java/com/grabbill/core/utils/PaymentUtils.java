package com.grabbill.core.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * @author michaellow
 */
public class PaymentUtils {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");

    public static String decimalFormatPaymentAmount(final BigDecimal value) {
        return DECIMAL_FORMAT.format(value.doubleValue());
    }

}
