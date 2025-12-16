import { PageableModel } from '@grabbill/lib';

export class QueryUnsubscribedEmails {
  static readonly type = '[UnsubscribedEmail] QueryUnsubscribedEmails';

  constructor(public pageable?: PageableModel, public startDate?: Date, public endDate?: Date, public email?: string) {}
}

export class ResetUnsubscribedEmails {
  static readonly type = '[UnsubscribedEmail] ResetUnsubscribedEmails';
}

export class DeleteUnsubscribedEmail {
  static readonly type = '[UnsubscribedEmail] DeleteUnsubscribedEmail';

  constructor(public id: number) {}
}
