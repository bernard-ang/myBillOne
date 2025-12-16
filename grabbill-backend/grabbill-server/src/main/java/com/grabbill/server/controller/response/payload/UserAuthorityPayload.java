package com.grabbill.server.controller.response.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.grabbill.core.entity.*;
import com.grabbill.server.controller.response.ApiPayload;
import com.stripe.model.Address;
import com.stripe.model.Customer;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

/**
 * @author michaellow
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserAuthorityPayload implements ApiPayload {

    private String name;

    private String email;

    private String role;

    private Set<String> privileges;

    private boolean active;

    private boolean verified;

    private boolean guidedStepsViewed;

    private UserAccountPayload account;

    private AccountSubscriptionPayload subscription;

    private BillingInformationPayload billingInfo;

    private boolean paymentMethodRequired;

    private boolean paymentGracePeriodExceeded;

    private double outstandingAmount;

    private boolean google2FAEnabled;

    private boolean email2FAEnabled;


    public static UserAuthorityPayload from(
            final User user,
            final Customer customer,
            final AccountSubscription accountSubscription,
            final boolean paymentMethodRequired,
            final boolean paymentGracePeriodExceeded,
            final double outstandingAmount
    ) {

        UserAuthorityPayload instance = new UserAuthorityPayload();
        instance.name = user.getName();
        instance.email = user.getEmail();

        Role role = user.getRole();
        instance.role = role.getName();

        instance.privileges = new HashSet<>();
        for (Privilege privilege : role.getPrivileges()) {
            instance.privileges.add(privilege.getName());
        }

        instance.active = user.isActive();
        instance.verified = user.isVerified();
        instance.guidedStepsViewed = user.isGuidedStepsViewed();

        instance.account = UserAccountPayload.from(user.getAccount());

        if (accountSubscription != null) {
            instance.subscription = AccountSubscriptionPayload.from(accountSubscription);
        }
        instance.paymentMethodRequired = paymentMethodRequired;
        instance.paymentGracePeriodExceeded = paymentGracePeriodExceeded;

        if(customer != null) {
            instance.billingInfo = BillingInformationPayload.from(customer);
        }

        instance.outstandingAmount = outstandingAmount;

        instance.google2FAEnabled = user.isGoogle2FAEnabled();
        instance.email2FAEnabled = user.isEmail2FAEnabled();

        return instance;
    }

    public static UserAuthorityPayload from(final AdminUser user) {
        UserAuthorityPayload instance = new UserAuthorityPayload();
        instance.name = user.getName();
        instance.email = user.getEmail();
        instance.active = user.isActive();

        instance.google2FAEnabled = user.isGoogle2FAEnabled();
        instance.email2FAEnabled = user.isEmail2FAEnabled();

        return instance;
    }

}
