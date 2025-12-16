import { ProcessStatus } from './process-status';
import { BaseIndexRowModel } from './base-index-row.model';

export interface BaseRecordModel {
  id: number;
  name: string;
  priority: number;
  retryCount: number;
  message: string;
  status: ProcessStatus;
  indexRow: BaseIndexRowModel;
}
