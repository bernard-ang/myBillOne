import { BaseIndexRowModel, ProcessStatus } from '../../data';

export interface TransactionalEmailActivityRequestModel {
  scheduledTimestamp?: Date;
  name: string;
  emailFrom: string;
  emailFromName: string;
  emailSubject: string;
  emailContent: string;
  emailMjmlContent: string;
  status: ProcessStatus;
  sendWhatsAppMessage: boolean;
  indexRows: BaseIndexRowModel[];
}
