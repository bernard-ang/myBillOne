import { BaseActivityModel } from '../base-activity.model';

export interface DigitalFilingActivityModel extends BaseActivityModel {
  sftp?: boolean;
  sftpPath?: string;
}
