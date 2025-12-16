package com.grabbill.server.controller;

import com.grabbill.core.conf.DeploymentProperties;
import com.grabbill.core.entity.*;
import com.grabbill.core.exception.GrabbillException;
import com.grabbill.core.model.*;
import com.grabbill.core.model.whatsapp.response.ThirdPartyWebhookResponse;
import com.grabbill.core.service.*;
import com.grabbill.core.service.payment.CustomerPaymentMethodService;
import com.grabbill.core.service.payment.CustomerService;
import com.grabbill.core.service.payment.PaymentService;
import com.grabbill.core.service.whatsapp.WhatsAppService;
import com.grabbill.core.service.whatsapp.WhatsAppServiceException;
import com.grabbill.core.service.whatsapp.WhatsAppSession;
import com.grabbill.server.GrabbillServerErrorCode;
import com.grabbill.server.controller.request.*;
import com.grabbill.server.controller.response.ApiMessage;
import com.grabbill.server.controller.response.GrabbillApiResponse;
import com.grabbill.server.controller.response.GrabbillServerApiVersion;
import com.grabbill.server.controller.response.payload.*;
import com.grabbill.server.dto.GrabbillUserDetails;
import com.grabbill.server.exception.GrabbillServerException;
import com.grabbill.server.service.*;
import com.stripe.model.Customer;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Provides endpoint for default new user registration steps:
 * 1. /account/register             - to register new user account OR google sign-in (firebase) to create new user account
 * 2. /account/resend-verify-email  - verification email undelivered, trigger to resend
 * 2. /account/verify-email         - email verification - submit verification token to activate the account
 * 3. /account                      - to get / update account details
 * 4. /account/plan?preview=1 (POST)- to preview plan subscription summary before commit
 * 5. /account/plan (POST)          - to update plan subscription
 * 6. /account/plan (GET)           - to get plan subscription details
 * 7. /account/forget-password      - to send password reset email
 * 8. /account/reset-password       - to reset password for account
 *
 * @author michaellow
 */
@RestController
@RequestMapping("/account")
public class AccountController {

    @Value("${payment.test-mode:false}")
    private boolean paymentTestMode;

    @Autowired
    private DeploymentProperties deploymentProperties;
    @Autowired
    private AccountService accountService;
    @Autowired
    private AccountSubscriptionService accountSubscriptionService;
    @Autowired
    private AccountUsageStatisticService accountUsageStatisticService;
    @Autowired
    private AccountStatementService accountStatementService;
    @Autowired
    private AffiliateCodeService affiliateCodeService;
    @Autowired
    private AuditLogService auditLogService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private CustomerPaymentMethodService customerPaymentMethodService;
    @Autowired @Qualifier("userEmailAuthenticator")
    private EmailAuthenticator userEmailAuthenticator;
    @Autowired
    private InvoiceService invoiceService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private PaymentNotificationEmailService paymentNotificationEmailService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private ProRateCalculator proRateCalculator;
    @Autowired
    private PlanService planService;
    @Autowired
    private PlanSwitcherService planSwitcherService;
    @Autowired
    private PromoCodeService promoCodeService;
    @Autowired
    private UserRegistrationService userRegistrationService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserAccountEmailService userAccountEmailService;
    @Autowired
    private WhatsAppService whatsAppService;
    @Value("${whatsapp.webhook.url}")
    private String whatsappWebhookUrl;


    @Transactional
    @PostMapping(value = "/register")
    public ResponseEntity<GrabbillApiResponse> register(
            @Valid @RequestBody NewUserAccountRegistrationRequest request
    ) {
        if (userService.getByEmail(request.getEmail()).isPresent()) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB1001,
                    "Email already associated with another user account."
            );
        }

        User registeredUser;
        try {
            registeredUser = userRegistrationService.register(
                    request.getName(),
                    request.getEmail(),
                    request.getPassword(),
                    request.getAffiliateCode()
            );
        } catch (GrabbillException e) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB1035,
                    request.getAffiliateCode() + " is invalid."
            );
        }

        userAccountEmailService.sendAccountVerificationEmail(registeredUser);

        auditLogService.log(
                registeredUser.getAccount().getId(),
                Optional.empty(),
                Long.valueOf(registeredUser.getId()),
                getDomainType(),
                ActionType.REGISTER,
                registeredUser.getEmail(),
                registeredUser.getName()
        );

        GrabbillApiResponse response = new GrabbillApiResponse(
                GrabbillServerApiVersion.V1.getVersion(),
                new ApiMessage("User account for " + registeredUser.getEmail() + " created successfully.")
        );
        return ResponseEntity.ok().body(response);
    }

    @PostMapping(value = "/resend-verify-email")
    public ResponseEntity<GrabbillApiResponse> resendVerifyEmail(
            @Valid @RequestBody ResendVerifyEmailRequest request
    ) {
        User user = getUser(request.getEmail());
        if (user.isVerified()) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB0015,
                    "User account " + request.getEmail() + " already been verified."
            );
        }

        userAccountEmailService.sendAccountVerificationEmail(user);

        auditLogService.log(
                user.getAccount().getId(),
                Optional.empty(),
                Long.valueOf(user.getId()),
                getDomainType(),
                ActionType.SEND_VERIFY_EMAIL,
                user.getEmail(),
                user.getName()
        );

        GrabbillApiResponse response = new GrabbillApiResponse(
                GrabbillServerApiVersion.V1.getVersion(),
                new ApiMessage("Verify email " + user.getEmail() + " resent successfully.")
        );
        return ResponseEntity.ok().body(response);
    }

    @GetMapping()
    public ResponseEntity<GrabbillApiResponse> getUserAccount(
            @AuthenticationPrincipal GrabbillUserDetails userDetails
    ) {
        User user = getUser(userDetails.getUsername());
        UserAccountPayload userAccountPayload = UserAccountPayload.from(user.getAccount());

        GrabbillApiResponse response = new GrabbillApiResponse(
                GrabbillServerApiVersion.V1.getVersion(),
                userAccountPayload
        );
        return ResponseEntity.ok().body(response);
    }

    @Transactional
    @PostMapping()
    public ResponseEntity<GrabbillApiResponse> updateUserAccount(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @Valid @RequestBody UserAccountUpdateRequest request
    ) {
        User user = getUser(userDetails.getUsername());
        Account target = user.getAccount();
        target.setCompanyName(StringUtils.hasLength(request.getCompanyName()) ? request.getCompanyName() : null);
        target.setCompanyContactNo(StringUtils.hasLength(request.getCompanyContactNo()) ? request.getCompanyContactNo() : null);
        target.setAddrLine1(StringUtils.hasLength(request.getAddrLine1()) ? request.getAddrLine1() : null);
        target.setAddrLine2(StringUtils.hasLength(request.getAddrLine2()) ? request.getAddrLine2() : null);
        target.setCity(StringUtils.hasLength(request.getCity()) ? request.getCity() : null);
        target.setState(StringUtils.hasLength(request.getState()) ? request.getState() : null);
        target.setPostcode(StringUtils.hasLength(request.getPostcode()) ? request.getPostcode() : null);
        target.setCountry(StringUtils.hasLength(request.getCountry()) ? request.getCountry() : null);
        target.setCountryIsoCode(StringUtils.hasLength(request.getCountryIsoCode()) ? request.getCountryIsoCode() : null);
        target.setLogo(StringUtils.hasLength(request.getLogo()) ? request.getLogo() : null);

        // if account is WITHOUT affiliate code AND request contains affiliate code
        if (!StringUtils.hasLength(target.getAffiliateMasterCode()) && StringUtils.hasLength(request.getAffiliateCode())) {

            String affiliateCode = request.getAffiliateCode();
            String masterCode = affiliateCode;
            String subCode = null;
            if (affiliateCode.contains("-")) {
                String[] codes = affiliateCode.split("-");
                masterCode = codes.length > 0 ? codes[0] : affiliateCode;
                subCode = codes.length > 1 ? codes[1] : null;
            }

            target.setAffiliateMasterCode(masterCode);
            target.setAffiliateSubCode(subCode);

            affiliateCodeService.getByCode(masterCode).orElseThrow(
                    () -> new GrabbillServerException(GrabbillServerErrorCode.GRB1032, "Invalid affiliate code - " + affiliateCode + "!")
            );

        }

        Account account = accountService.save(target);

        auditLogService.log(
                userDetails.getUser().getAccount().getId(),
                Optional.empty(),
                Long.valueOf(account.getId()),
                getDomainType(),
                ActionType.UPDATE,
                user.getAccount().getCompanyName(),
                userDetails.getUsername()
        );

        GrabbillApiResponse response = new GrabbillApiResponse(
                GrabbillServerApiVersion.V1.getVersion(),
                UserAccountPayload.from(account)
        );
        return ResponseEntity.ok().body(response);
    }

    @Transactional
    @GetMapping(value = "/statements")
    public ResponseEntity<GrabbillApiResponse> getAccountStatements(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate,
            @PageableDefault(sort = {"startDate"}, direction = Sort.Direction.ASC) Pageable pageable
    ) {
        User user = getUser(userDetails.getUsername());
        Account account = user.getAccount();

        Page<AccountStatement> page = accountStatementService.getAllByAccountId(account.getId(), startDate, endDate, pageable);
        SearchResultPayload<AccountStatementPayload> searchResultPayload =
                SearchResultPayload.<AccountStatementPayload>builder()
                        .items(page.get().map(AccountStatementPayload::from).collect(Collectors.toList()))
                        .totalItems(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .build();

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        searchResultPayload
                )
        );
    }

    @Transactional
    @PutMapping(path = "/waba")
    public ResponseEntity<GrabbillApiResponse> updateWaba(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @RequestBody UserAccountUpdateWabaRequest request
    ) {
        verifyIfOnPremDeployment();
        User user = getUser(userDetails.getUsername());
        Account target = user.getAccount();

        target.setWabaEmail(request.getWabaEmail());
        if (request.getWabaPassword() != null) {
            target.setWabaPassword(request.getWabaPassword());
        }
        target.setWabaId(request.getWabaId());
        target.setWabaGuid(request.getWabaGuid());
        target.setWabaName(request.getWabaName());
        target.setWabaPhone(request.getWabaPhone());
        target.setWabaPhoneId(request.getWabaPhoneId());

        Account account = accountService.save(target);
        auditLogService.log(
                userDetails.getUser().getAccount().getId(),
                Optional.empty(),
                Long.valueOf(account.getId()),
                getDomainType(),
                ActionType.WABA_UPDATE,
                "WhatsApp Business Account information was updated.",
                userDetails.getUsername()
        );

        GrabbillApiResponse response = new GrabbillApiResponse(
                GrabbillServerApiVersion.V1.getVersion(),
                UserAccountWithWabaDetailsPayload.from(account)
        );
        return ResponseEntity.ok().body(response);
    }

    @Transactional
    @GetMapping(path = "/waba/login")
    public ResponseEntity<GrabbillApiResponse> loginWaba(
            @AuthenticationPrincipal GrabbillUserDetails userDetails
    ) {
        verifyIfOnPremDeployment();
        User user = getUser(userDetails.getUsername());
        Account target = user.getAccount();

        WhatsAppSession session = whatsAppService.login(
                target.getWabaGuid(),
                target.getWabaEmail(),
                target.getWabaPassword()
        );

        if (session == null) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB1501,
                    "Account with ID [" + target + "] WABA login failed"
            );
        }

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        new ApiMessage("WABA login successfully.")
                )
        );
    }

    @Transactional
    @GetMapping(path = "/waba/register-webhook")
    public ResponseEntity<GrabbillApiResponse> registerWebhook(
            @AuthenticationPrincipal GrabbillUserDetails userDetails
    ) {
        verifyIfOnPremDeployment();
        User user = getUser(userDetails.getUsername());
        Account account = user.getAccount();

        try {
            WhatsAppSession session = whatsAppService.login(account.getWabaGuid(), account.getWabaEmail(), account.getWabaPassword());

            String webhookUrl = whatsappWebhookUrl.replace("{ACCOUNT_ID}", account.getId().toString());
            ThirdPartyWebhookResponse thirdPartyWebhookResponse =
                    session.registerWebHook(account.getId().toString(), webhookUrl);

            account.setWabaWebhookId(thirdPartyWebhookResponse.getId());
            account.setWabaWebhookUrl(webhookUrl);
            Account updatedAccount = accountService.save(account);

            auditLogService.log(
                    userDetails.getUser().getAccount().getId(),
                    Optional.empty(),
                    Long.valueOf(updatedAccount.getId()),
                    getDomainType(),
                    ActionType.WABA_WEBHOOK_REGISTERED,
                    "WhatsApp webhook was registered.",
                    userDetails.getUsername()
            );

            return ResponseEntity.ok().body(
                    new GrabbillApiResponse(
                            GrabbillServerApiVersion.V1.getVersion(),
                            UserAccountWithWabaDetailsPayload.from(updatedAccount)
                    )
            );
        } catch (WhatsAppServiceException exception) {
            throw handleWhatsappError(exception);
        }
    }

    @Transactional
    @GetMapping(path = "/waba/unregister-webhook")
    public ResponseEntity<GrabbillApiResponse> unregisterWebhook(
            @AuthenticationPrincipal GrabbillUserDetails userDetails
    ) {
        verifyIfOnPremDeployment();
        User user = getUser(userDetails.getUsername());
        Account account = user.getAccount();

        try {
            WhatsAppSession session = whatsAppService.login(account.getWabaGuid(), account.getWabaEmail(), account.getWabaPassword());

            session.unregisterWebHook(account.getWabaWebhookId());

            account.setWabaWebhookId(null);
            account.setWabaWebhookUrl(null);
            Account updatedAccount = accountService.save(account);

            auditLogService.log(
                    userDetails.getUser().getAccount().getId(),
                    Optional.empty(),
                    Long.valueOf(updatedAccount.getId()),
                    getDomainType(),
                    ActionType.WABA_WEBHOOK_UNREGISTERED,
                    "WhatsApp webhook was unregistered.",
                    userDetails.getUsername()
            );

            return ResponseEntity.ok().body(
                    new GrabbillApiResponse(
                            GrabbillServerApiVersion.V1.getVersion(),
                            UserAccountWithWabaDetailsPayload.from(updatedAccount)
                    )
            );
        } catch (WhatsAppServiceException exception) {
            throw handleWhatsappError(exception);
        }
    }

    @PostMapping("/verify-email")
    public ResponseEntity<GrabbillApiResponse> verifyEmail(
            @Valid @RequestBody NewUserAccountEmailVerificationRequest request
    ) {
        User user = getUser(request.getEmail());
        if (!user.getVerificationCode().equals(request.getVerificationCode())) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB1009,
                    "Failed to verify user [" + request.getEmail()
                            + "] with invalid verification code [" + request.getVerificationCode() + "]!"
            );
        }

        user.setActive(true);
        user.setVerified(true);
        user.setVerificationCode(RandomStringUtils.randomAlphanumeric(15));
        userService.save(user);

        auditLogService.log(
                user.getAccount().getId(),
                Optional.empty(),
                Long.valueOf(user.getId()),
                getDomainType(),
                ActionType.VERIFY_EMAIL,
                user.getEmail(),
                user.getName()
        );

        GrabbillApiResponse response = new GrabbillApiResponse(
                GrabbillServerApiVersion.V1.getVersion(),
                new ApiMessage("User account for " + request.getEmail() + " is verified successfully.")
        );

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/plan")
    public ResponseEntity<GrabbillApiResponse> getPlanDetails(
            @AuthenticationPrincipal GrabbillUserDetails userDetails
    ) {
        User user = getUser(userDetails.getUsername());
        Account account = user.getAccount();
        Optional<AccountSubscription> activeSubscription = accountSubscriptionService.getActiveSubscriptionByAccountId(account.getId());
        AccountSubscriptionPayload subscriptionPayload = null;
        if (activeSubscription.isPresent()) {
            subscriptionPayload = AccountSubscriptionPayload.from(activeSubscription.get());
        }

        GrabbillApiResponse response = new GrabbillApiResponse(
                GrabbillServerApiVersion.V1.getVersion(),
                subscriptionPayload != null ? subscriptionPayload : new ApiMessage("No subscribed plan.")
        );
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/forget-password")
    public ResponseEntity<GrabbillApiResponse> forgetPassword(
            @Valid @RequestBody ForgetPasswordRequest request
    ) {
        User user = getUser(request.getEmail());
        userAccountEmailService.sendForgetPasswordEmail(user);

        auditLogService.log(
                user.getAccount().getId(),
                Optional.empty(),
                Long.valueOf(user.getId()),
                getDomainType(),
                ActionType.FORGET_PASSWORD,
                user.getEmail(),
                user.getName()
        );

        GrabbillApiResponse response = new GrabbillApiResponse(
                GrabbillServerApiVersion.V1.getVersion(),
                new ApiMessage("Forget password email " + user.getEmail() + " sent successfully.")
        );
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<GrabbillApiResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        User user = getUser(request.getEmail());
        if (!user.getVerificationCode().equals(request.getCode())) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB1009,
                    "Failed to reset user [" + request.getEmail()
                            + "] password with invalid code [" + request.getCode() + "]!"
            );
        }

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setVerificationCode(RandomStringUtils.randomAlphanumeric(15));
        userService.save(user);

        auditLogService.log(
                user.getAccount().getId(),
                Optional.empty(),
                Long.valueOf(user.getId()),
                getDomainType(),
                ActionType.RESET_PASSWORD,
                user.getEmail(),
                user.getName()
        );

        GrabbillApiResponse response = new GrabbillApiResponse(
                GrabbillServerApiVersion.V1.getVersion(),
                new ApiMessage("Password for " + request.getEmail() + " is reset successfully.")
        );

        return ResponseEntity.ok().body(response);
    }

    @Transactional
    @PostMapping("/generate-email-otp")
    public ResponseEntity<GrabbillApiResponse> generateEmailOtp(
            @Valid @RequestBody EmailOtpRequest request
    ) {
        User user = getUser(request.getEmail());
        int otp = userEmailAuthenticator.generateOTP(user.getEmail());
        user.setEmail2FAOtp(otp);
        user.setEmail2FAOtpRequestedTime(OffsetDateTime.now(ZoneOffset.UTC));
        user = userService.save(user);

        userAccountEmailService.sendEmailOtpEmail(user, otp);

        auditLogService.log(
                user.getAccount().getId(),
                Optional.empty(),
                Long.valueOf(user.getId()),
                getDomainType(),
                ActionType.GENERATE_EMAIL_OTP,
                user.getEmail(),
                user.getName()
        );

        GrabbillApiResponse response = new GrabbillApiResponse(
                GrabbillServerApiVersion.V1.getVersion(),
                new ApiMessage("OTP email for " + user.getEmail() + " sent successfully.")
        );
        return ResponseEntity.ok().body(response);
    }

    @Transactional
    @PostMapping("/generate-email-2fa-activation-otp")
    public ResponseEntity<GrabbillApiResponse> generateEmail2faActivationOtp(
            @Valid @RequestBody EmailOtpRequest request
    ) {
        User user = getUser(request.getEmail());
        int otp = userEmailAuthenticator.generateOTP(user.getEmail());
        user.setEmail2FAOtp(otp);
        user.setEmail2FAOtpRequestedTime(OffsetDateTime.now(ZoneOffset.UTC));
        user = userService.save(user);

        userAccountEmailService.sendEmail2faActivationOtpEmail(user, otp);

        auditLogService.log(
                user.getAccount().getId(),
                Optional.empty(),
                Long.valueOf(user.getId()),
                getDomainType(),
                ActionType.GENERATE_EMAIL_2FA_ACTIVATION_OTP,
                user.getEmail(),
                user.getName()
        );

        GrabbillApiResponse response = new GrabbillApiResponse(
                GrabbillServerApiVersion.V1.getVersion(),
                new ApiMessage("OTP email 2fa activation for " + user.getEmail() + " sent successfully.")
        );
        return ResponseEntity.ok().body(response);
    }

    @Transactional
    @PostMapping(value = "/plan", params = { "preview=1" })
    public ResponseEntity<GrabbillApiResponse> previewPlanSwitchSummary(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @Valid @RequestBody UserPlanUpdateRequest request
    ) {
        OffsetDateTime targetEndDate = OffsetDateTime.now(ZoneOffset.UTC);
        User user = getUser(userDetails.getUsername());
        Account account = user.getAccount();
        Plan plan = getPlan(request.getPlanId());
        AccountSubscription selectedNewSubscription = planSwitcherService.toNewAccountSubscriptionTransient(account, request, plan);
        PromoCode promoCode = getPromoCode(request.getPromoCode());

        Customer customer = customerService.getOrCreate(account);
        com.stripe.model.PaymentMethod defaultPaymentMethod = customerPaymentMethodService.getDefaultPaymentMethodByCustomerId(customer.getId());
        boolean hasPaymentMethod = defaultPaymentMethod != null;


        double oldAmountCharged = 0;
        double newAmountChargeable = 0;
        double offsetAmount = 0;
        long remainingDays = 0;

        newAmountChargeable = getSubscriptionChargeableAmount(selectedNewSubscription);
        AccountSubscription currentSubscription = accountSubscriptionService.getActiveSubscriptionByAccountId(account.getId()).orElse(null);
        if (currentSubscription != null) {
            oldAmountCharged = getSubscriptionChargeableAmount(currentSubscription);

            remainingDays = proRateCalculator.calculateRemainingDays(currentSubscription, targetEndDate);
            offsetAmount = Math.min(proRateCalculator.calculateOffset(currentSubscription, targetEndDate), newAmountChargeable);
        }


        BillingInformation billingInformation = customerService.getBillingInformation(customer, defaultPaymentMethod);
        Invoice targetInvoice = invoiceService.createNewTransient(user, null, account, selectedNewSubscription, offsetAmount, promoCode);


        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        PlanSelectionInvoicePayload.from(
                                targetInvoice,
                                currentSubscription,
                                oldAmountCharged,
                                newAmountChargeable,
                                remainingDays,
                                hasPaymentMethod,
                                billingInformation != null,
                                promoCode
                        )
                )
        );
    }

    @Transactional
    @PostMapping(value = "/plan")
    public ResponseEntity<GrabbillApiResponse> switchPlan(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @Valid @RequestBody UserPlanUpdateRequest request
    ) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        User user = getUser(userDetails.getUsername());
        Account account = user.getAccount();
        Plan plan = getPlan(request.getPlanId());
        AccountSubscription currentAccountSubscription = getActiveSubscription(account.getId());
        AccountUsageStatistic accountUsageStatistic = getAccountUsageStatistic(account.getId());
        PromoCode promoCode = getPromoCode(request.getPromoCode());

        Customer customer = customerService.getOrCreate(account);
        com.stripe.model.PaymentMethod defaultPaymentMethod = customerPaymentMethodService.getDefaultPaymentMethodByCustomerId(customer.getId());
        boolean hasPaymentMethod = defaultPaymentMethod != null;


        // validate before plan switch
        validateBeforePlanSwitch(
                account,
                request,
                planSwitcherService.toNewAccountSubscriptionTransient(account, request, plan),
                currentAccountSubscription,
                accountUsageStatistic,
                hasPaymentMethod
        );


        double offsetAmount = 0;
        AccountSubscription newAccountSubscription = planSwitcherService.toNewAccountSubscriptionPersistent(account, request, plan, promoCode);
        // 1. terminate / end current subscription if present
        if (currentAccountSubscription != null) {
            currentAccountSubscription = planSwitcherService.terminateAccountSubscription(currentAccountSubscription, now);
            double newAmountChargeable = getSubscriptionChargeableAmount(newAccountSubscription);
            offsetAmount = Math.min(proRateCalculator.calculateOffset(currentAccountSubscription, now), newAmountChargeable);
        }


        // 2. update account usage statistic & reset email statistic
        planSwitcherService.updateNewPlanLimitsToAccountUsageStatistic(accountUsageStatistic, newAccountSubscription);


        // 3. create invoice for billing, with offsetAmount
        BillingInformation billingInformation = customerService.getBillingInformation(customer, defaultPaymentMethod);


        // TODO: relook on this again!!!
        // if not chargeable (NON-FREE plan), should also send invoice and generate invoice?
        Invoice transientInvoice = invoiceService.createNewTransient(
                user, billingInformation, account, newAccountSubscription, offsetAmount, promoCode);
        boolean chargeable = transientInvoice.getTotalAmountWithTax() > 0;


        // 4. create invoice and auto-charge if PAID plan
        if (chargeable) {
            StoragePlanOption storageOption;
            TransactionalEmailPlanOption transactionalEmailOption;
            EmailCampaignPlanOption emailCampaignOption;

            if (paymentTestMode) {
                storageOption = new StoragePlanOption();
                storageOption.setId(99L);
                transactionalEmailOption = new TransactionalEmailPlanOption();
                transactionalEmailOption.setId(99L);
                emailCampaignOption = new EmailCampaignPlanOption();
                emailCampaignOption.setId(99L);

            } else {
                storageOption = (StoragePlanOption) planService.getBasePlanOption(
                        newAccountSubscription.getStorageSize(), plan.getStorageOptions(), "storage");
                transactionalEmailOption = (TransactionalEmailPlanOption) planService.getBasePlanOption(
                        newAccountSubscription.getTransactionalEmailSize(), plan.getTransactionalEmailOptions(), "transactional email");
                emailCampaignOption = (EmailCampaignPlanOption) planService.getBasePlanOption(
                        newAccountSubscription.getEmailCampaignSize(), plan.getEmailCampaignOptions(), "email campaign");
            }

            com.stripe.model.Invoice stripeInvoice = paymentService.createInvoice(
                    account,
                    newAccountSubscription,
                    !paymentTestMode ? plan.getId() : 99,
                    request.getSubscriptionMode(),
                    storageOption,
                    transactionalEmailOption,
                    emailCampaignOption,
                    offsetAmount,
                    promoCode
            );
            stripeInvoice = paymentService.chargeInvoice(stripeInvoice.getId());

            Invoice targetInvoice = invoiceService.createNewPersistent(
                    user, stripeInvoice, billingInformation, account, newAccountSubscription, offsetAmount, promoCode);

            paymentNotificationEmailService.sendEmail(targetInvoice, stripeInvoice, promoCode);
        }

        auditLogService.log(
                userDetails.getUser().getAccount().getId(),
                Optional.empty(),
                Long.valueOf(account.getId()),
                DomainType.PLAN,
                ActionType.SUBSCRIBE,
                plan.getName(),
                userDetails.getUsername()
        );
        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        SwitchPlanAccountSubscriptionPayload.from(newAccountSubscription, hasPaymentMethod)
                )
        );
    }

    @Transactional
    @GetMapping(value = "/subscriptions")
    public ResponseEntity<GrabbillApiResponse> getSubscriptions(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PageableDefault(sort = {"createdDate"}, direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Account account = userDetails.getUser().getAccount();
        Page<AccountSubscription> page = accountSubscriptionService.getSubscriptionsByAccount(account, pageable);

        SearchResultPayload<AccountSubscriptionPayload> searchResultPayload =
                SearchResultPayload.<AccountSubscriptionPayload>builder()
                        .items(page.get()
                                .map(AccountSubscriptionPayload::from)
                                .collect(Collectors.toList()))
                        .totalItems(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .build();

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        searchResultPayload
                )
        );
    }

    @Transactional
    @PutMapping(path = "/sftp")
    public ResponseEntity<GrabbillApiResponse> updateSftpSettings(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @RequestBody UserAccountUpdateSftpSettingsRequest request
    ) {
        verifyIfOnPremDeployment();
        User user = getUser(userDetails.getUsername());
        Account target = user.getAccount();

        target.setSftpHost(request.getHost());
        target.setSftpPort(request.getPort());
        target.setSftpUsername(request.getUsername());
        target.setSftpPassword(request.getPassword());

        Account account = accountService.save(target);
        auditLogService.log(
                userDetails.getUser().getAccount().getId(),
                Optional.empty(),
                Long.valueOf(account.getId()),
                getDomainType(),
                ActionType.SFTP_UPDATE,
                "SFTP setting was updated.",
                userDetails.getUsername()
        );

        GrabbillApiResponse response = new GrabbillApiResponse(
                GrabbillServerApiVersion.V1.getVersion(),
                UserAccountPayload.from(account)
        );
        return ResponseEntity.ok().body(response);
    }

    private void validateBeforePlanSwitch(
            final Account account,
            final UserPlanUpdateRequest request,
            final AccountSubscription newAccountSubscription,
            final AccountSubscription currentAccountSubscription,
            final AccountUsageStatistic accountUsageStatistic,
            final boolean hasPaymentMethod
    ) {
        // disallow switch plan if any unpaidInvoices
        if (!invoiceService.getUnpaidInvoices(account).isEmpty()) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB1028,
                    "Account has unpaid invoice(s)!"
            );
        }

        // new plan is chargeable while no payment method registered
        if (!isFreePlan(newAccountSubscription) && !hasPaymentMethod) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB8003,
                    "Account has no registered payment method!"
            );
        }

        // disallow downgrade plan if storage usage is exceeding the new storage size
        if ((currentAccountSubscription != null)
                && (currentAccountSubscription.getStorageSize() > request.getStorageSize())
                && (accountUsageStatistic.getTotalStorageUsed() > request.getStorageSize())
        ) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB1024,
                    "Account with id [" + account.getId() + "] is using storage size larger than the new limit!"
            );
        }
    }

    private boolean isFreePlan(final AccountSubscription accountSubscription) {
        return (accountSubscription.getStoragePrice()
                + accountSubscription.getEmailCampaignPrice()
                + accountSubscription.getTransactionalEmailPrice()) == 0;
    }

    private User getUser(final String email) {
        return userService.getByEmail(email).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB1002,
                        "User [" + email + "] is not found!"
                )
        );
    }

    private AccountSubscription getActiveSubscription(final Integer accountId) {
        return accountSubscriptionService.getActiveSubscriptionByAccountId(accountId).orElse(null);
    }

    private double getSubscriptionChargeableAmount(final AccountSubscription accountSubscription) {
        return SubscriptionMode.MONTHLY.equals(accountSubscription.getMode()) ?
                accountSubscriptionService.getMonthlyRate(accountSubscription) :
                accountSubscriptionService.getAnnuallyRate(accountSubscription);
    }

    private AccountUsageStatistic getAccountUsageStatistic(final Integer accountId) {
        return accountUsageStatisticService.getByAccountId(accountId).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB1020,
                        "Account with id [" + accountId + "] has no usage statistic!"
                )
        );
    }

    private Plan getPlan(final String planId) {
        return planService.getById(planId).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB1004, "Invalid plan id [" + planId + "]!")
        );
    }

    private PromoCode getPromoCode(final String code) {
        if (StringUtils.hasLength(code)) {
            PromoCode promoCode = promoCodeService.getByCode(code).orElseThrow(
                    () -> new GrabbillServerException(
                            GrabbillServerErrorCode.GRB1037, "Invalid promo code [" + code + "]!")
            );

            if (promoCode.isActive()) {
                OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
                if (promoCode.getStart() == null && promoCode.getEnd() == null) {
                    return promoCode;
                } else if (promoCode.getStart() != null && now.isAfter(promoCode.getStart()) && promoCode.getEnd() == null) {
                    return promoCode;
                } else if (promoCode.getStart() == null && promoCode.getEnd() != null && now.isBefore(promoCode.getEnd())) {
                    return promoCode;
                } else if (promoCode.getStart() != null && now.isAfter(promoCode.getStart())
                        && promoCode.getEnd() != null && now.isBefore(promoCode.getEnd())) {
                    return promoCode;
                }
            }

            throw new GrabbillServerException(GrabbillServerErrorCode.GRB1037, "Invalid promo code [" + code + "]!");
        }

        return null;
    }

    // TODO: AOP-fied this method
    private void verifyIfOnPremDeployment() {
        if (!deploymentProperties.isOnPremise()) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB0016,
                    "Current deployment mode is [" + deploymentProperties.getMode() + "]!"
            );
        }
    }

    private static GrabbillServerException handleWhatsappError(WhatsAppServiceException exception) {
        GrabbillServerErrorCode errorCode;
        switch (exception.getErrorCode()) {
            case GRB1501:
                errorCode = GrabbillServerErrorCode.GRB1501;
                break;
            case GRB1502:
                errorCode = GrabbillServerErrorCode.GRB1502;
                break;
            case GRB1503:
                errorCode = GrabbillServerErrorCode.GRB1503;
                break;
            case GRB1504:
                errorCode = GrabbillServerErrorCode.GRB1504;
                break;
            case GRB1505:
                errorCode = GrabbillServerErrorCode.GRB1505;
                break;
            case GRB1506:
                errorCode = GrabbillServerErrorCode.GRB1506;
                break;
            case GRB1507:
                errorCode = GrabbillServerErrorCode.GRB1507;
                break;
            default:
                errorCode = GrabbillServerErrorCode.GRB1500;
        }

        return new GrabbillServerException(errorCode, exception.getMessage());
    }

    public DomainType getDomainType() {
        return DomainType.ACCOUNT;
    }

}
