import { StripeEventType } from './stripe-event-type';

export interface StripeEventBasicModel {
  id: number;
  eventId: string;
  eventType: string;
  type: StripeEventType;
  refId: string;

  accountId: number;
  accountName: string;

  createdDate: Date;
  createdBy: string;
}
