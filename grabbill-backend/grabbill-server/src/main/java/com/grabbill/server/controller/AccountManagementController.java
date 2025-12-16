package com.grabbill.server.controller;

import com.grabbill.core.entity.*;
import com.grabbill.core.model.AdminActionType;
import com.grabbill.core.model.AdminDomainType;
import com.grabbill.core.model.SubscriptionMode;
import com.grabbill.core.model.plan.*;
import com.grabbill.core.service.*;
import com.grabbill.core.service.payment.CustomerPaymentMethodService;
import com.grabbill.core.service.payment.CustomerService;
import com.grabbill.core.service.whatsapp.WhatsAppServiceException;
import com.grabbill.server.GrabbillServerErrorCode;
import com.grabbill.server.controller.request.AdminAccountPaymentExemptionUpdateRequest;
import com.grabbill.server.controller.request.AdminAccountStatusUpdateRequest;
import com.grabbill.server.controller.request.AdminAccountWabaRequest;
import com.grabbill.server.controller.request.UserPlanUpdateRequest;
import com.grabbill.server.controller.response.ApiMessage;
import com.grabbill.server.controller.response.GrabbillApiResponse;
import com.grabbill.server.controller.response.GrabbillServerApiVersion;
import com.grabbill.server.controller.response.payload.*;
import com.grabbill.server.dto.GrabbillAdminUserDetails;
import com.grabbill.server.exception.GrabbillServerException;
import com.grabbill.server.controller.response.payload.whatsapp.TemplatesPayload;
import com.grabbill.core.model.whatsapp.response.ThirdPartyWebhookResponse;
import com.grabbill.server.service.AccountReportService;
import com.grabbill.server.service.PlanSwitcherService;
import com.grabbill.core.service.whatsapp.WhatsAppService;
import com.grabbill.core.service.whatsapp.WhatsAppSession;
import com.stripe.model.Customer;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author michaellow
 */
@Slf4j
@RestController
@RequestMapping("/mgmt/accounts")
public class AccountManagementController extends BaseManagementController {

    @Autowired
    private AccountReportService accountReportService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private AccountSubscriptionService accountSubscriptionService;
    @Autowired
    private AccountUsageStatisticService accountUsageStatisticService;
    @Autowired
    private AccountStatementService accountStatementService;
    @Autowired
    private AdminAuditLogService adminAuditLogService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private CustomerPaymentMethodService customerPaymentMethodService;
    @Autowired
    private PlanService planService;
    @Autowired
    private PlanSwitcherService planSwitcherService;
    @Autowired
    private PlanUsageService planUsageService;
    @Autowired
    private ProRateCalculator proRateCalculator;
    @Autowired
    private WhatsAppService whatsAppService;

    @Value("${whatsapp.webhook.url}")
    private String whatsappWebhookUrl;


    @Transactional
    @GetMapping()
    public ResponseEntity<GrabbillApiResponse> getAccounts(
            @AuthenticationPrincipal GrabbillAdminUserDetails userDetails,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String planName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate,
            @PageableDefault(sort = {"lastModifiedDate"}, direction = Sort.Direction.DESC) Pageable pageable
    ) {
        checkStatus(userDetails);

        Page<Account> page = accountService.searchAccount(name, planName, startDate, endDate, pageable);

        SearchResultPayload<AccountBasicPayload> searchResultPayload =
                SearchResultPayload.<AccountBasicPayload>builder()
                        .items(page.get()
                                .map(account -> {
                                    Optional<User> userOptional = getAccountOwner(account);
                                    Optional<AccountSubscription> accountSubscriptionOptional =
                                            account.getSubscriptions()
                                                    .stream()
                                                    .filter(accountSubscription -> accountSubscription.getEndDate() == null)
                                                    .findFirst();

                                    return AccountBasicPayload.from(account, userOptional, accountSubscriptionOptional);
                                })
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
    @GetMapping(path = "/{accountId}")
    public ResponseEntity<GrabbillApiResponse> getAccountDetails(
            @AuthenticationPrincipal GrabbillAdminUserDetails userDetails,
            @PathVariable Integer accountId
    ) {
        checkStatus(userDetails);

        Account account = accountService.getById(accountId).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB1021,
                        "Account with ID [" + accountId + "] is not found!"
                )
        );

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        getAccountDetailsPayload(account)
                )
        );
    }

    @Transactional
    @GetMapping(path = "/{accountId}/users")
    public ResponseEntity<GrabbillApiResponse> getAccountUsers(
            @AuthenticationPrincipal GrabbillAdminUserDetails userDetails,
            @PathVariable Integer accountId
    ) {
        checkStatus(userDetails);

        Account account = accountService.getById(accountId).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB1021,
                        "Account with ID [" + accountId + "] is not found!"
                )
        );

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        AccountUsersPayload.from(account.getUsers())
                )
        );
    }

    @Transactional
    @GetMapping(path = "/{accountId}/statements")
    public ResponseEntity<GrabbillApiResponse> getAccountStatements(
            @AuthenticationPrincipal GrabbillAdminUserDetails userDetails,
            @PathVariable Integer accountId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate,
            @PageableDefault(sort = {"startDate"}, direction = Sort.Direction.ASC) Pageable pageable
    ) {
        checkStatus(userDetails);
        Account account = accountService.getById(accountId).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB1021,
                        "Account with ID [" + accountId + "] is not found!"
                )
        );

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
    @PutMapping(path = "/{accountId}/status")
    public ResponseEntity<GrabbillApiResponse> updateAccountStatus(
            @AuthenticationPrincipal GrabbillAdminUserDetails userDetails,
            @PathVariable Integer accountId,
            @RequestBody AdminAccountStatusUpdateRequest request
    ) {
        checkStatus(userDetails);

        Account account = accountService.getById(accountId).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB1021,
                        "Account with ID [" + accountId + "] is not found!"
                )
        );

        for (User user : account.getUsers()) {
            user.setAccountActive(request.isActive());
        }
        Account updatedAccount = accountService.save(account);

        AdminAuditLog adminAuditLog = new AdminAuditLog();
        adminAuditLog.setAdminDomainType(AdminDomainType.ACCOUNT);
        adminAuditLog.setTargetId(account.getId());
        adminAuditLog.setActionType(request.isActive() ? AdminActionType.ACCOUNT_ACTIVATE.name() : AdminActionType.ACCOUNT_DEACTIVATE.name());
        adminAuditLog.setDescription("Account with id [" + account.getId() + "] was " + (request.isActive() ? "activated." : "deactivated."));
        adminAuditLogService.save(adminAuditLog);

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        AccountUsersPayload.from(updatedAccount.getUsers())
                )
        );
    }

    @Transactional
    @PutMapping(path = "/{accountId}/waba")
    public ResponseEntity<GrabbillApiResponse> updateWaba(
            @AuthenticationPrincipal GrabbillAdminUserDetails userDetails,
            @PathVariable Integer accountId,
            @RequestBody AdminAccountWabaRequest request
    ) {
        checkStatus(userDetails);

        Account account = accountService.getById(accountId).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB1021,
                        "Account with ID [" + accountId + "] is not found!"
                )
        );

        account.setWabaEmail(request.getWabaEmail());
        if (request.getWabaPassword() != null) {
            account.setWabaPassword(request.getWabaPassword());
        }
        account.setWabaId(request.getWabaId());
        account.setWabaGuid(request.getWabaGuid());
        account.setWabaName(request.getWabaName());
        account.setWabaPhone(request.getWabaPhone());
        account.setWabaPhoneId(request.getWabaPhoneId());

        Account updatedAccount = accountService.save(account);

        AdminAuditLog adminAuditLog = new AdminAuditLog();
        adminAuditLog.setAdminDomainType(AdminDomainType.ACCOUNT);
        adminAuditLog.setTargetId(account.getId());
        adminAuditLog.setActionType(AdminActionType.ACCOUNT_WABA_UPDATE.name());
        adminAuditLog.setDescription("Account WABA with id [" + account.getId() + "] was updated.");
        adminAuditLogService.save(adminAuditLog);

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        getAccountDetailsPayload(updatedAccount)
                )
        );
    }

    @Transactional
    @GetMapping(path = "/{accountId}/waba/login")
    public ResponseEntity<GrabbillApiResponse> login(
            @AuthenticationPrincipal GrabbillAdminUserDetails userDetails,
            @PathVariable Integer accountId
    ) {
        checkStatus(userDetails);

        Account account = accountService.getById(accountId).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB1021,
                        "Account with ID [" + accountId + "] is not found!"
                )
        );

        WhatsAppSession session = whatsAppService.login(account.getWabaGuid(), account.getWabaEmail(), account.getWabaPassword());

        if (session == null) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB1501,
                    "Account with ID [" + accountId + "] WABA login failed"
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
    @GetMapping(path = "/{accountId}/waba/templates")
    public ResponseEntity<GrabbillApiResponse> getTemplates(
            @AuthenticationPrincipal GrabbillAdminUserDetails userDetails,
            @PathVariable Integer accountId
    ) {
        checkStatus(userDetails);

        Account account = accountService.getById(accountId).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB1021,
                        "Account with ID [" + accountId + "] is not found!"
                )
        );

        WhatsAppSession session = whatsAppService.login(account.getWabaGuid(), account.getWabaEmail(), account.getWabaPassword());

        if (session == null) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB1501,
                    "Account with ID [" + accountId + "] WABA login failed"
            );
        }


        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        TemplatesPayload.from(session.refreshTemplate())
                )
        );
    }

    @Transactional
    @GetMapping(path = "/{accountId}/waba/register-webhook")
    public ResponseEntity<GrabbillApiResponse> registerWebhook(
            @AuthenticationPrincipal GrabbillAdminUserDetails userDetails,
            @PathVariable Integer accountId
    ) {
        checkStatus(userDetails);

        Account account = accountService.getById(accountId).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB1021,
                        "Account with ID [" + accountId + "] is not found!"
                )
        );

        try {
            WhatsAppSession session = whatsAppService.login(account.getWabaGuid(), account.getWabaEmail(), account.getWabaPassword());

            String webhookUrl = whatsappWebhookUrl.replace("{ACCOUNT_ID}", accountId.toString());
            ThirdPartyWebhookResponse thirdPartyWebhookResponse =
                    session.registerWebHook(accountId.toString(), webhookUrl);

            account.setWabaWebhookId(thirdPartyWebhookResponse.getId());
            account.setWabaWebhookUrl(webhookUrl);
            Account updatedAccount = accountService.save(account);

            AdminAuditLog adminAuditLog = new AdminAuditLog();
            adminAuditLog.setAdminDomainType(AdminDomainType.ACCOUNT);
            adminAuditLog.setTargetId(account.getId());
            adminAuditLog.setActionType(AdminActionType.ACCOUNT_WABA_WEBHOOK_REGISTERED.name());
            adminAuditLog.setDescription("Account with id [" + account.getId() + "] webhook was registered.");
            adminAuditLogService.save(adminAuditLog);

            return ResponseEntity.ok().body(
                    new GrabbillApiResponse(
                            GrabbillServerApiVersion.V1.getVersion(),
                            getAccountDetailsPayload(updatedAccount)
                    )
            );
        } catch (WhatsAppServiceException exception) {
            throw handleWhatsappError(exception);
        }
    }

    @Transactional
    @GetMapping(path = "/{accountId}/waba/unregister-webhook")
    public ResponseEntity<GrabbillApiResponse> unregisterWebhook(
            @AuthenticationPrincipal GrabbillAdminUserDetails userDetails,
            @PathVariable Integer accountId
    ) {
        checkStatus(userDetails);

        Account account = accountService.getById(accountId).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB1021,
                        "Account with ID [" + accountId + "] is not found!"
                )
        );

        try {
            WhatsAppSession session = whatsAppService.login(account.getWabaGuid(), account.getWabaEmail(), account.getWabaPassword());

            session.unregisterWebHook(account.getWabaWebhookId());

            account.setWabaWebhookId(null);
            account.setWabaWebhookUrl(null);
            Account updatedAccount = accountService.save(account);

            AdminAuditLog adminAuditLog = new AdminAuditLog();
            adminAuditLog.setAdminDomainType(AdminDomainType.ACCOUNT);
            adminAuditLog.setTargetId(account.getId());
            adminAuditLog.setActionType(AdminActionType.ACCOUNT_WABA_WEBHOOK_UNREGISTERED.name());
            adminAuditLog.setDescription("Account with id [" + account.getId() + "] webhook was unregistered.");
            adminAuditLogService.save(adminAuditLog);

            return ResponseEntity.ok().body(
                    new GrabbillApiResponse(
                            GrabbillServerApiVersion.V1.getVersion(),
                            getAccountDetailsPayload(updatedAccount)
                    )
            );
        } catch (WhatsAppServiceException exception) {
            throw handleWhatsappError(exception);
        }
    }

    @Transactional
    @PutMapping(path = "/{accountId}/payment-exemption-status")
    public ResponseEntity<GrabbillApiResponse> updateAccountPaymentExemptionStatus(
            @AuthenticationPrincipal GrabbillAdminUserDetails userDetails,
            @PathVariable Integer accountId,
            @RequestBody AdminAccountPaymentExemptionUpdateRequest request
    ) {
        checkStatus(userDetails);

        Account account = accountService.getById(accountId).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB1021,
                        "Account with ID [" + accountId + "] is not found!"
                )
        );

        account.setPaymentExempted(request.isPaymentExempted());
        Account updatedAccount = accountService.save(account);

        AdminAuditLog adminAuditLog = new AdminAuditLog();
        adminAuditLog.setAdminDomainType(AdminDomainType.ACCOUNT);
        adminAuditLog.setTargetId(updatedAccount.getId());
        adminAuditLog.setActionType(request.isPaymentExempted() ?
                AdminActionType.ACCOUNT_SET_PAYMENT_EXEMPTED.name() :
                AdminActionType.ACCOUNT_UNSET_PAYMENT_EXEMPTED.name());
        adminAuditLog.setDescription("Account with id [" + updatedAccount.getId() + "] was " +
                (request.isPaymentExempted() ? " set as payment exempted." : " unset from payment exempted."));
        adminAuditLogService.save(adminAuditLog);

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        getAccountDetailsPayload(updatedAccount)
                )
        );
    }

    @Transactional
    @PutMapping(path = "/{accountId}/plan")
    public ResponseEntity<GrabbillApiResponse> switchPlan(
            @AuthenticationPrincipal GrabbillAdminUserDetails userDetails,
            @PathVariable Integer accountId,
            @RequestBody UserPlanUpdateRequest request
    ) {
        checkStatus(userDetails);

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        Account account = getAccount(accountId);
        Plan plan = getPlan(request.getPlanId());
        AccountSubscription currentAccountSubscription = getActiveSubscription(account.getId());
        AccountUsageStatistic accountUsageStatistic = getAccountUsageStatistic(account.getId());

        Customer customer = customerService.getOrCreate(account);
        com.stripe.model.PaymentMethod defaultPaymentMethod = customerPaymentMethodService.getDefaultPaymentMethodByCustomerId(customer.getId());


        // pre-validate:
        validateBeforePlanSwitch(
                account,
                request,
                currentAccountSubscription,
                accountUsageStatistic
        );

        // mark account as payment exempted if it is NOT
        if (!account.isPaymentExempted()) {
            account.setPaymentExempted(true);
            account = accountService.save(account);
        }

        // 1. terminate / end current subscription
        currentAccountSubscription = planSwitcherService.terminateAccountSubscription(currentAccountSubscription, now);
        AccountSubscription newAccountSubscription = planSwitcherService.toNewAccountSubscriptionPersistent(account, request, plan, null);
        double totalAmountChargeable = getSubscriptionChargeableAmount(newAccountSubscription);
        double offsetAmount = Math.min(proRateCalculator.calculateOffset(currentAccountSubscription, now), totalAmountChargeable);


        // 2. update account usage statistic & reset email statistic
        planSwitcherService.updateNewPlanLimitsToAccountUsageStatistic(accountUsageStatistic, newAccountSubscription);


        AdminAuditLog adminAuditLog = new AdminAuditLog();
        adminAuditLog.setAdminDomainType(AdminDomainType.ACCOUNT);
        adminAuditLog.setTargetId(account.getId());
        adminAuditLog.setActionType(AdminActionType.ACCOUNT_SWITCH_PLAN.name());
        adminAuditLog.setDescription("Account with id [" + account.getId() + "] has switched to plan - " + newAccountSubscription.getPlanName());
        adminAuditLogService.save(adminAuditLog);

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        getAccountDetailsPayload(account)
                )
        );
    }

    @Transactional
    @GetMapping(value = "/reports")
    public ResponseEntity<ByteArrayResource> downloadReport(
            @AuthenticationPrincipal GrabbillAdminUserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate,
            @RequestParam(required = false) String affiliateCode,
            @RequestParam String tz
    ) {
        checkStatus(userDetails);

        ZoneId zoneId = ZoneId.of(tz);
        List<Account> accounts = accountService.getAccountsBetween(startDate, endDate, affiliateCode);

        Workbook workbook = accountReportService.generateAccountsReport(accounts, zoneId);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            workbook.write(baos);
            return new ResponseEntity<>(new ByteArrayResource(baos.toByteArray()), createReportDownloadHttpHeaders(baos.toByteArray().length, "accounts-report.xlsx"), HttpStatus.OK);

        } catch (IOException e) {
            throw new GrabbillServerException(GrabbillServerErrorCode.GRB8301, "Failed to generate accounts report for period [" + startDate + " - " + endDate + "].");

        } finally {
            try {
                baos.close();
            } catch (IOException e) {
                log.warn("Failed to close ByteArrayOutputStream during accounts report generation");
            }
        }
    }

    private AccountDetailsPayload getAccountDetailsPayload(Account account) {
        User owner = getAccountOwner(account).get();

        String planName = null;
        StorageUsage storageUsage = null;
        TransactionalEmailUsage transactionalEmailUsage = null;
        EmailCampaignUsage emailCampaignUsage = null;
        SmsUsage smsUsage = null;
        WhatsappUsage whatsappUsage = null;

        Optional<AccountSubscription> currentSubscriptionOptional =
                accountSubscriptionService.getActiveSubscriptionByAccountId(account.getId());
        if (currentSubscriptionOptional.isPresent()) {
            AccountSubscription currentSubscription = currentSubscriptionOptional.get();
            planName = currentSubscription.getPlanName();
            storageUsage = planUsageService.calcStorageUsage(account, currentSubscription);
            transactionalEmailUsage = planUsageService.calcTransactionalEmailUsage(account, currentSubscription);
            emailCampaignUsage = planUsageService.calcEmailCampaignUsage(account, currentSubscription);

            AccountUsageStatistic accountUsageStatistic = getAccountUsageStatistic(account.getId());
            smsUsage = SmsUsage.from(accountUsageStatistic);
            whatsappUsage = WhatsappUsage.from(accountUsageStatistic);
        }


        return AccountDetailsPayload.from(
                account,
                owner,
                planName,
                storageUsage,
                transactionalEmailUsage,
                emailCampaignUsage,
                smsUsage,
                whatsappUsage
        );
    }

    private Account getAccount(final Integer accountId) {
        return accountService.getById(accountId).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB1021,
                        "Account with ID [" + accountId + "] is not found!"
                )
        );
    }

    private Optional<User> getAccountOwner(final Account account) {
        return account.getUsers()
                .stream()
                .filter(user -> user.getRole().getName().equals("OWNER"))
                .findFirst();
    }

    private void validateBeforePlanSwitch(
            final Account account,
            final UserPlanUpdateRequest request,
            final AccountSubscription accountSubscription,
            final AccountUsageStatistic accountUsageStatistic
    ) {
        // disallow downgrade plan if storage usage is exceeding the new storage size
        if ((accountSubscription.getStorageSize() > request.getStorageSize())
                && accountUsageStatistic.getTotalStorageUsed() > request.getStorageSize()) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB1024,
                    "Account with id [" + account.getId() + "] is using storage size larger than the new limit!"
            );
        }
    }

    private AccountSubscription getActiveSubscription(final Integer accountId) {
        return accountSubscriptionService.getActiveSubscriptionByAccountId(accountId).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB1020,
                        "Account with id [" + accountId + "] has no active subscription plan!"
                )
        );
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

    private HttpHeaders createReportDownloadHttpHeaders(
            final int dataLength,
            final String reportName
    ) {
        List<String> exposeHeaders = new ArrayList<>();
        exposeHeaders.add(HttpHeaders.CONTENT_DISPOSITION);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccessControlExposeHeaders(exposeHeaders);
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + reportName + "\"");
        headers.setContentLength(dataLength);

        return headers;
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

}
