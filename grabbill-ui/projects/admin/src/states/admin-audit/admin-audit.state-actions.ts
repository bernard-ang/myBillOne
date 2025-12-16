import { PageableModel } from '@grabbill/lib';

export class ResetAudits {
  static readonly type = '[Admin Audit] ResetAudits';
}

export class QueryAudits {
  static readonly type = '[Admin Audit] QueryAudits';

  constructor(public pageable?: PageableModel, public query?: string, public startDate?: Date, public endDate?: Date) {}
}
