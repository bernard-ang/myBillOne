import { ProtocolEncryption } from '../data';

export interface MailServerUpdateRequestModel {
  customServer: boolean;
  smtpHost: string;
  smtpPort: number;
  smtpEncryption: ProtocolEncryption;
  smtpUsername: string;
  smtpPassword: string;

  smtpFrom: string;
  smtpFromName: string;

  imapHost: string;
  imapPort: number;
  imapEncryption: ProtocolEncryption;
  imapUsername: string;
  imapPassword: string;
}
