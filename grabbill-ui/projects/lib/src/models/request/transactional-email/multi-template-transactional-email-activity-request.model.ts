import { BaseIndexRowModel, MultiTemplateTransactionalEmailTemplateModel, ProcessStatus } from "../../data";

export interface MultiTemplateTransactionalEmailActivityRequestModel {
  scheduledTimestamp?: Date;
  name: string;
  emailFrom: string;
  emailFromName: string;
  status: ProcessStatus;
  sendWhatsAppMessage: boolean;
  indexRows: BaseIndexRowModel[];
  sftp: boolean;
  sftpPath: string;
  transactionalEmailActivityTemplates: MultiTemplateTransactionalEmailTemplateModel[];
}
