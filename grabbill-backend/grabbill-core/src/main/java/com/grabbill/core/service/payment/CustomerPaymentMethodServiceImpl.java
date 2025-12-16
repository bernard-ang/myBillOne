package com.grabbill.core.service.payment;

import com.grabbill.core.exception.GrabbillException;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import com.stripe.model.PaymentMethodCollection;
import com.stripe.param.CustomerListPaymentMethodsParams;

import java.util.List;

/**
 * @author michaellow
 */
public class CustomerPaymentMethodServiceImpl implements CustomerPaymentMethodService {

    @Override
    public List<PaymentMethod> getByCustomerId(final String customerId) {
        try {
            return getInternal(getCustomer(customerId)).getData();
        } catch (StripeException e) {
            throw new GrabbillException(
                    "Failed to retrieve payment methods for customer [" + customerId + "] from payment platform", e);
        }
    }

    @Override
    public PaymentMethod getDefaultPaymentMethodByCustomerId(final String customerId) {
        Customer customer;
        List<PaymentMethod> paymentMethods;
        try {
            customer = getCustomer(customerId);
            paymentMethods = getInternal(customer).getData();
        } catch (StripeException e) {
            throw new GrabbillException(
                    "Failed to retrieve payment methods for customer [" + customerId + "] from payment platform", e);
        }

        String defaultPaymentId = customer.getInvoiceSettings().getDefaultPaymentMethod();
        for (PaymentMethod paymentMethod : paymentMethods) {
            if (paymentMethod.getId().equals(defaultPaymentId)) {
                return paymentMethod;
            }
        }

        return null;
    }

    private Customer getCustomer(final String customerId) throws StripeException {
        return Customer.retrieve(customerId);
    }

    private PaymentMethodCollection getInternal(final Customer customer) throws StripeException {
        CustomerListPaymentMethodsParams params = CustomerListPaymentMethodsParams.builder()
                        .setType(CustomerListPaymentMethodsParams.Type.CARD)
                        .build();

        return customer.listPaymentMethods(params);
    }

}
