export interface TransactionalEmailActivityCreateRequestModel {
  name: string;
  emailFrom: string;
  emailFromName: string;
  emailSubject: string;
  emailContent: string;
  emailMjmlContent: string;
}
