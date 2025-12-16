export interface IndexRowUsageModel {
  email: string;
  mobileNo: string;
  validMobileNo: boolean;
  totalBytes: number;
  totalCredits: number;
  smsContent: string;
}

export interface SmsActivityCreditUsagePayloadModel {
  indexRowUsages: IndexRowUsageModel[];
  totalSms: number;
  estimatedCreditUsage: number[];
}
