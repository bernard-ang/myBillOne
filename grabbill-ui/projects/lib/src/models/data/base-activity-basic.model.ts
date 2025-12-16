import { ProcessStatus } from "./process-status";
import { AuditableModel } from "./common";

export interface BaseActivityBasicModel extends AuditableModel {
  id: number;
  name: string;
  status: ProcessStatus;
  noOfFiles: number;
  priority: number;
  retryCount: number;
  message: string;
  draftTimestamp: Date;
  submittedTimestamp: Date;
  processingTimestamp: Date;
  processedTimestamp: Date;
  errorTimestamp: Date;
  purgedBy: string;
  purgedTimestamp: Date;
  expectedPurgedTimestamp: Date;
}
