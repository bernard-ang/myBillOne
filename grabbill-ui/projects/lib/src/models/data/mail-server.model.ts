import { ProtocolEncryption } from './protocol-encryption';

export interface MailServerModel {
  id: number;
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
