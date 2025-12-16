import { DomainType } from './domain-type';
import { JobExecutionMode } from './job-execution-mode';
import { JobStatus } from './job-status';
import { JobEventType } from './job-event-type';

export interface JobModel {
  id: number;
  executionMode: JobExecutionMode;
  eventType: JobEventType;
  domainType: DomainType;
  accountId: number;
  accountName: string;
  typeId: number;
  typeName: string;
  activityId: number;
  activityName: string;
  status: JobStatus;
  reason: string;
  createdBy: string;
  createdTimestamp: Date;
  scheduledExecutionTimestamp: Date;
  executionStartTimestamp: Date;
  executionEndTimestamp: Date;
  digitalFiling?: {
    totalRecords: number;
  };
  transactionalEmail?: {
    totalRecords: number;
    totalRecordsSent: number;
    totalRecordsBounced: number;
    totalRecordsSkipped: number;

    fromEmail: string;
    fromName: string;
    subject: string;
    content: string;

    whatsAppTemplateName: string;
    whatsAppBodyContent: string;
    whatsAppDocument: boolean;
    whatsAppFooterContent: string;
    whatsAppButton: string;

    whatsAppStatusSent: number;
    whatsAppStatusSkip: number;
    whatsAppStatusRead: number;
    whatsAppStatusDelivered: number;
    whatsAppStatusAcknowledge: number;
    whatsAppStatusFailed: number;
  };
  emailCampaign?: {
    totalRecords: number;
    totalRecordsSent: number;
    totalRecordsBounced: number;
    totalRecordsSkipped: number;

    fromEmail: string;
    fromName: string;
    subject: string;
    content: string;
  };
  whatsApp?: {
    whatsAppTemplateName: string;
    whatsAppBodyContent: string;
    whatsAppDocument: boolean;
    whatsAppFooterContent: string;
    whatsAppButton: string;

    totalRecords: number;
    totalRecordsSent: number;
    totalRecordsDelivered: number;
    totalRecordsSkipped: number;
    totalRecordsRead: number;
    totalRecordsAcknowledged: number;
    totalRecordsFailed: number;
  };
  sms?: {
    smsContent: string;
    smsFrom: string;
    totalCreditUsed: number;
    totalRecords: number;
    totalRecordsError: number;
    totalRecordsSent: number;
  };
}
