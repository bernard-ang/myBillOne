package com.grabbill.core.service.payment;

import com.grabbill.core.entity.Account;
import com.grabbill.core.entity.BillingInformation;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;

/**
 * @author michaello
 */
public interface CustomerService {

    Customer get(Account account);

    Customer getOrCreate(Account account);

    BillingInformation getBillingInformation(Customer customer, PaymentMethod paymentMethod);

}
