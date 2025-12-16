import { PageableModel } from '@grabbill/lib';

export class QueryAudits {
  static readonly type = '[Audit] QueryAudits';

  constructor(public pageable?: PageableModel, public query?: string, public startDate?: Date, public endDate?: Date) {}
}

export class ResetAudits {
  static readonly type = '[Audit] ResetAudits';
}
