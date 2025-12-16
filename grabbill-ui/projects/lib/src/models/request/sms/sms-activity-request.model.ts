import { BaseIndexRowModel, ProcessStatus } from "../../data";

export interface SmsActivityRequestModel {
  scheduledTimestamp?: Date;
  name: string;
  smsFrom: string;
  smsContent: string;
  contactGroupId?: number;
  status: ProcessStatus;
  indexRows: BaseIndexRowModel[];
}
