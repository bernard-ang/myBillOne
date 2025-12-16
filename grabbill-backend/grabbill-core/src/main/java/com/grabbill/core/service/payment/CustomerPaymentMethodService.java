package com.grabbill.core.service.payment;

import com.stripe.model.PaymentMethod;

import java.util.List;

/**
 * @author michaellow
 */
public interface CustomerPaymentMethodService {

    List<PaymentMethod> getByCustomerId(String customerId);

    PaymentMethod getDefaultPaymentMethodByCustomerId(String customerId);

}
