import { ProcessStatus } from "./process-status";
import { BaseFileModel } from "./base-file.model";
import { BaseRecordModel } from "./base-record.model";
import { BaseIndexRowModel } from "./base-index-row.model";
import { AuditableModel } from "./common";

export interface BaseActivityModel extends AuditableModel {
  id: number;
  name: string;
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
  status: ProcessStatus;
  files: BaseFileModel[];
  indexRows: BaseIndexRowModel[];
  records: BaseRecordModel[];
}
