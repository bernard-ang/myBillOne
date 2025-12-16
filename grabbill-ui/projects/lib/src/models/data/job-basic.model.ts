import { DomainType } from "./domain-type";
import { JobStatus } from "./job-status";
import { JobEventType } from "./job-event-type";
import { JobExecutionMode } from "./job-execution-mode";

export interface JobBasicModel {
  id: number;
  domainType: DomainType;
  executionMode: JobExecutionMode;
  eventType: JobEventType;
  scheduledExecutionTimestamp: Date;
  accountId: number;
  accountName: string;
  typeId: number;
  typeName: string;
  activityId: number;
  activityName: string;
  status: JobStatus;
  reason: string;
  createdTimestamp: Date;
}
