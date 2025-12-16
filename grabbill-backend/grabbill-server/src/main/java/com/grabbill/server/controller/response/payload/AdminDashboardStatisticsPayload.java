package com.grabbill.server.controller.response.payload;

import com.grabbill.server.controller.response.ApiPayload;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author michaellow
 */
@Data
public class AdminDashboardStatisticsPayload implements ApiPayload {

    private int totalAccount;
    private long totalContacts;
    private double totalMonthlySubscriptionFees;
    private long totalStorageUsed;

    private long totalStorageAllocated;
    private int totalTransactionalEmailSent;

    private int totalCampaignEmailSent;
    private int totalActivitySubmitted;
    private int totalActivityProcessed;

    private List<KeyToCount> accountsByPlan = new ArrayList<>();
    private List<KeyToCount> accountsByCountry = new ArrayList<>();


    @AllArgsConstructor
    @Data
    static class KeyToCount {
        private String key;
        private Integer count;
    }


    public static AdminDashboardStatisticsPayload from(
            final int totalAccount,
            final long totalContacts,
            final double totalMonthlySubscriptionFees,
            final long totalStorageUsed,
            final long totalStorageAllocated,
            final int totalTransactionalEmailSent,
            final int totalCampaignEmailSent,
            final int totalActivitySubmitted,
            final int totalActivityProcessed,
            final Map<String, Integer> planNameToAccountCountMap,
            final Map<String, Integer> countryToAccountCountMap
    ) {
        AdminDashboardStatisticsPayload payload = new AdminDashboardStatisticsPayload();
        payload.totalAccount = totalAccount;
        payload.totalContacts = totalContacts;
        payload.totalMonthlySubscriptionFees = totalMonthlySubscriptionFees;
        payload.totalStorageUsed = totalStorageUsed;
        payload.totalStorageAllocated = totalStorageAllocated;
        payload.totalTransactionalEmailSent = totalTransactionalEmailSent;
        payload.totalCampaignEmailSent = totalCampaignEmailSent;
        payload.totalActivitySubmitted = totalActivitySubmitted;
        payload.totalActivityProcessed = totalActivityProcessed;

        for (Map.Entry<String, Integer> entry : planNameToAccountCountMap.entrySet()) {
            payload.accountsByPlan.add(
                    new KeyToCount(entry.getKey(), entry.getValue())
            );
        }
        for (Map.Entry<String, Integer> entry : countryToAccountCountMap.entrySet()) {
            payload.accountsByCountry.add(
                    new KeyToCount(entry.getKey(), entry.getValue())
            );
        }

        return payload;
    }

}
