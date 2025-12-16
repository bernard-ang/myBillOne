import { BaseIndexRowModel, ProcessStatus } from '../../data';

export interface WhatsAppActivityRequestModel {
  scheduledTimestamp?: Date;
  name: string;
  status: ProcessStatus;
  indexRows: BaseIndexRowModel[];

  // multiple template
  sftp?: boolean;
  sftpPath?: string;
}
