package com.grabbill.server.controller;

import com.grabbill.core.entity.*;
import com.grabbill.core.service.*;
import com.grabbill.server.controller.response.GrabbillApiResponse;
import com.grabbill.server.controller.response.GrabbillServerApiVersion;
import com.grabbill.server.controller.response.payload.AdminDashboardStatisticsPayload;
import com.grabbill.server.dto.GrabbillAdminUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * @author michaellow
 */
@RestController
@RequestMapping("/mgmt/dashboard")
public class AdminDashboardController extends BaseManagementController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountSubscriptionService accountSubscriptionService;

    @Autowired
    private AccountUsageStatisticService accountUsageStatisticService;

    @Autowired
    private ContactService contactService;

    @Autowired
    @Qualifier("digitalFilingActivityService")
    private BaseActivityService<DigitalFilingType, DigitalFilingActivity> dfaService;

    @Autowired
    @Qualifier("transactionalEmailActivityService")
    private BaseActivityService<TransactionalEmailType, TransactionalEmailActivity> txeaService;

    @Autowired
    @Qualifier("emailCampaignActivityService")
    private BaseActivityService<EmailCampaignType, EmailCampaignActivity> ecaService;


    @Transactional
    @GetMapping(value = "/current-statistics")
    public ResponseEntity<GrabbillApiResponse> loadCurrentStatistics(
            @AuthenticationPrincipal GrabbillAdminUserDetails userDetails
    ) {
        checkStatus(userDetails);

        int totalAccount = 0;
        long totalContacts = 0;
        double totalMonthlySubscriptionFees = 0;
        long totalStorageUsed = 0;
        long totalStorageAllocated = 0;
        int totalTransactionalEmailSent = 0;
        int totalEmailCampaignEmailSent = 0;

        int totalActivitySubmitted = 0;
        int totalActivityProcessed = 0;
        Map<String, Integer> planNameToAccountCountMap = new HashMap<>();
        Map<String, Integer> countryToAccountCountMap = new HashMap<>();

        totalContacts = contactService.count();


        Set<Integer> accountIdsWithActivePlan = new HashSet<>();
        List<AccountSubscription> accountSubscriptionList = accountSubscriptionService.getAllActiveSubscriptions();
        for (AccountSubscription accountSubscription : accountSubscriptionList) {
            Account account = accountSubscription.getAccount();
            accountIdsWithActivePlan.add(account.getId());
            totalAccount += 1;

            double totalFee = accountSubscription.getStoragePrice() + accountSubscription.getTransactionalEmailPrice() + accountSubscription.getEmailCampaignPrice();
            totalMonthlySubscriptionFees += totalFee;

            Optional<AccountUsageStatistic> accountUsageStatisticOptional = accountUsageStatisticService.getByAccountId(account.getId());
            if (accountUsageStatisticOptional.isPresent()) {
                AccountUsageStatistic accountUsageStatistic = accountUsageStatisticOptional.get();

                totalStorageUsed += accountUsageStatistic.getTotalStorageUsed();
                totalStorageAllocated += accountUsageStatistic.getMaxStorageSize();
            }

            if (!planNameToAccountCountMap.containsKey(accountSubscription.getPlanName())) {
                planNameToAccountCountMap.put(
                        accountSubscription.getPlanName(),
                        1
                );
            } else {
                planNameToAccountCountMap.put(
                        accountSubscription.getPlanName(),
                        planNameToAccountCountMap.get(accountSubscription.getPlanName()) + 1
                );
            }

            if (account.getCountryIsoCode() != null) {
                if (!countryToAccountCountMap.containsKey(account.getCountryIsoCode())) {
                    countryToAccountCountMap.put(
                            account.getCountryIsoCode(),
                            1
                    );

                } else {
                    countryToAccountCountMap.put(
                            account.getCountryIsoCode(),
                            countryToAccountCountMap.get(account.getCountryIsoCode()) + 1
                    );
                }
            }
        }

        List<Account> accountsWithoutActivePlan = accountService.getByIdNotIn(accountIdsWithActivePlan);
        planNameToAccountCountMap.put("NO_PLAN", accountsWithoutActivePlan.size());
        for (Account account : accountsWithoutActivePlan) {
            totalAccount += 1;
            if (account.getCountryIsoCode() != null) {
                if (!countryToAccountCountMap.containsKey(account.getCountryIsoCode())) {
                    countryToAccountCountMap.put(account.getCountryIsoCode(), 1);

                } else {
                    countryToAccountCountMap.put(account.getCountryIsoCode(), countryToAccountCountMap.get(account.getCountryIsoCode()) + 1);
                }
            }
        }

        totalActivitySubmitted = (dfaService.countAllSubmittedActivities() + txeaService.countAllSubmittedActivities()) + ecaService.countAllSubmittedActivities();

        List<DigitalFilingActivity> processedDfActivities = dfaService.getAllProcessedActivities();
        List<TransactionalEmailActivity> processedTxeActivities = txeaService.getAllProcessedActivities();
        List<EmailCampaignActivity> processedEcActivities = ecaService.getAllProcessedActivities();
        totalActivityProcessed += (processedDfActivities.size() + processedTxeActivities.size() + processedEcActivities.size());

        for (TransactionalEmailActivity processedTxeActivity : processedTxeActivities) {
            totalTransactionalEmailSent += processedTxeActivity.getEmailStatusSent();
        }

        for (EmailCampaignActivity processedEcActivity : processedEcActivities) {
            totalEmailCampaignEmailSent += processedEcActivity.getEmailStatusSent();
        }


        AdminDashboardStatisticsPayload payload = AdminDashboardStatisticsPayload.from(
                totalAccount,
                totalContacts,
                totalMonthlySubscriptionFees,
                totalStorageUsed,
                totalStorageAllocated,
                totalTransactionalEmailSent,
                totalEmailCampaignEmailSent,
                totalActivitySubmitted,
                totalActivityProcessed,
                planNameToAccountCountMap,
                countryToAccountCountMap
        );

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        payload
                )
        );
    }

}
