export interface UnsubscribedEmailModel {
  id: number;
  domainType: string;
  email: string;
  typeId: number;
  typeName: string;
  activityId: number;
  activityName: string;
  reason: string;
  createdBy: string;
  createdDate: Date;
}
