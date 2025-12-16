export interface KeyToCount {
  key: string;
  count: number;
}

export interface AdminDashboardStatisticsModel {
  totalAccount: number;
  totalContacts: number;
  totalMonthlySubscriptionFees: number;
  totalStorageUsed: number;
  totalStorageAllocated: number;
  totalTransactionalEmailSent: number;
  totalCampaignEmailSent: number;
  totalActivitySubmitted: number;
  totalActivityProcessed: number;

  accountsByPlan: KeyToCount[];
  accountsByCountry: KeyToCount[];
}
