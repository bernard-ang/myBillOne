import { BaseActivityModel } from '../base-activity.model';
import { TransactionalEmailRecordModel } from './transactional-email-record.model';
import { EmbeddedLinkModel } from '../embedded-link.model';
import { MultiTemplateTransactionalEmailTemplateModel } from "./multi-template-transactional-email-template.model";

export interface MultiTemplateTransactionalEmailActivityModel extends BaseActivityModel {
  scheduledTimestamp?: Date;

  emailFrom: string;
  emailFromName: string;
  emailAttachmentFileName?: string;

  smsContent?: string;

  emailTotal: number;
  emailStatusBounced: number;
  emailStatusBouncedSkip: number;
  emailStatusOpened: number;
  emailStatusSent: number;
  emailStatusUnsubscribed: number;
  emailStatusUnsubscribedSkip: number;

  sendWhatsAppMessage: boolean;
  whatsAppTemplateName: string;
  whatsAppBodyContent: string;
  whatsAppDocument: boolean;
  whatsAppFooterContent: string;
  whatsAppButton: string;

  records: TransactionalEmailRecordModel[];

  embeddedLinks: EmbeddedLinkModel[];

  sftp?: boolean;
  sftpPath?: string;

  mtTransactionalEmailActivityTemplates: MultiTemplateTransactionalEmailTemplateModel[];
}
