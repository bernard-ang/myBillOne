import { PageableModel } from '@grabbill/lib';

export class ResetStripEvents {
  static readonly type = '[Stripe Event Management] ResetStripEvents';
}

export class QueryStripeEvents {
  static readonly type = '[Stripe Event Management] QueryStripeEvents';

  constructor(
    public pageable: PageableModel,
    public accountName?: string,
    public type?: string,
    public refId?: string,
    public startDate?: Date,
    public endDate?: Date
  ) {}
}

export class ResetStripeEvent {
  static readonly type = '[Stripe Event Management] ResetStripeEvent';
}

export class GetStripeEvent {
  static readonly type = '[Stripe Event Management] GetStripeEvent';

  constructor(public id: number) {}
}
