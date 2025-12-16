import { BaseIndexRowModel, ProcessStatus } from '@grabbill/lib';

export interface DigitalFilingActivityRequestModel {
  name: string;
  sftp: boolean;
  sftpPath: string;
  status: ProcessStatus;
  indexRows: BaseIndexRowModel[];
}
