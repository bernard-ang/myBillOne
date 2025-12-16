import { PageableModel } from '@grabbill/lib';

export class QueryBouncedEmails {
  static readonly type = '[BouncedEmail] QueryBouncedEmails';

  constructor(public pageable?: PageableModel, public endDate?: Date, public startDate?: Date, public email?: string) {}
}

export class ResetBouncedEmails {
  static readonly type = '[BouncedEmail] ResetBouncedEmails';
}

export class DeleteBouncedEmail {
  static readonly type = '[BouncedEmail] DeleteBouncedEmail';

  constructor(public id: number) {}
}
