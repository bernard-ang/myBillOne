package com.grabbill.core.service.payment;

import com.grabbill.core.entity.Account;
import com.grabbill.core.entity.BillingInformation;
import com.grabbill.core.entity.User;
import com.grabbill.core.exception.GrabbillException;
import com.grabbill.core.service.AccountService;
import com.grabbill.core.service.UserService;
import com.stripe.exception.StripeException;
import com.stripe.model.Address;
import com.stripe.model.Customer;
import com.stripe.model.CustomerSearchResult;
import com.stripe.model.PaymentMethod;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerSearchParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

/**
 * @author michaellow
 */
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private AccountService accountService;
    @Autowired
    private UserService userService;


    @Override
    public Customer get(final Account account) {
        Customer targetCustomer;
        try {
            targetCustomer = getInternal(userService.getAccountOwner(account));
        } catch (StripeException e) {
            throw new GrabbillException(
                    "Failed to retrieve customer instance [" + account.getStripeCustomerId() + "] from payment platform", e);
        }

        return targetCustomer;
    }

    @Override
    public Customer getOrCreate(final Account account) {
        Customer targetCustomer = get(account);

        if (targetCustomer == null) {
            User accountOwner = userService.getAccountOwner(account);
            try {
                targetCustomer = Customer.create(
                        CustomerCreateParams.builder()
                                .setName(accountOwner.getEmail())
                                .setEmail(accountOwner.getEmail())
                                .setDescription(accountOwner.getName())
                                .setBalance(0L)
                                .build()
                );

                Account targetAccount = accountService.getById(account.getId()).get();
                targetAccount.setStripeCustomerId(targetCustomer.getId());
                accountService.save(targetAccount);

            } catch (StripeException e) {
                throw new GrabbillException(
                        "Failed to create customer instance [" + account.getStripeCustomerId() + "] from payment platform", e);
            }
        } else if (!StringUtils.hasLength(account.getStripeCustomerId())) {
            Account targetAccount = accountService.getById(account.getId()).get();
            targetAccount.setStripeCustomerId(targetCustomer.getId());
            accountService.save(targetAccount);
        }

        return targetCustomer;
    }

    @Override
    public BillingInformation getBillingInformation(final Customer customer, final PaymentMethod paymentMethod) {
        BillingInformation billingInformation = null;
        if (customer.getAddress() != null) {
            billingInformation = new BillingInformation();
            billingInformation.setName(customer.getName());
            billingInformation.setContactNo(customer.getPhone());
            billingInformation.setEmail(customer.getEmail());

            Address address = customer.getAddress();
            billingInformation.setAddrLine1(address.getLine1());
            billingInformation.setAddrLine2(address.getLine2());
            billingInformation.setCity(address.getCity());
            billingInformation.setState(address.getState());
            billingInformation.setPostcode(address.getPostalCode());
            billingInformation.setCountry(address.getCountry());
            billingInformation.setCountryIsoCode(address.getCountry());
            billingInformation.setDefaultBillingInfo(true);

        } else if (paymentMethod != null && paymentMethod.getBillingDetails() != null) {
            billingInformation = new BillingInformation();
            PaymentMethod.BillingDetails billingDetails = paymentMethod.getBillingDetails();
            billingInformation.setName(StringUtils.hasLength(customer.getName()) ? customer.getName() : billingInformation.getName());
            billingInformation.setContactNo(StringUtils.hasLength(customer.getPhone()) ? customer.getPhone() : billingInformation.getContactNo());
            billingInformation.setEmail(StringUtils.hasLength(customer.getEmail()) ? customer.getEmail() : billingInformation.getEmail());

            Address address = billingDetails.getAddress();
            billingInformation.setAddrLine1(address.getLine1());
            billingInformation.setAddrLine2(address.getLine2());
            billingInformation.setCity(address.getCity());
            billingInformation.setState(address.getState());
            billingInformation.setPostcode(address.getPostalCode());
            billingInformation.setCountry(address.getCountry());
            billingInformation.setCountryIsoCode(address.getCountry());
            billingInformation.setDefaultBillingInfo(true);
        }

        return billingInformation;
    }

    private Customer getInternal(final User accountOwner) throws StripeException {
        CustomerSearchResult customerSearchResult = Customer.search(
                CustomerSearchParams.builder()
                        .setQuery("email:'" + accountOwner.getEmail() + "'")
                        .setLimit(1L)
                        .build()
        );

        return !customerSearchResult.getData().isEmpty() ? customerSearchResult.getData().get(0) : null;
    }

}
