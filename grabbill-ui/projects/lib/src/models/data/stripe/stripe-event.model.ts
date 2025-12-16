import { StripeEventType } from './stripe-event-type';

export interface StripeEventModel {
  id: number;
  eventId: string;
  eventType: string;
  type: StripeEventType;
  refId: string;
  jsonObject: string;

  accountId: number;
  accountName: string;

  createdDate: Date;
  createdBy: string;
}
