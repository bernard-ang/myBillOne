import { PageableModel } from '@grabbill/lib';

export class ResetAccountStatements {
  static readonly type = '[Usage] Reset Account Statements';
}

export class QueryAccountStatements {
  static readonly type = '[Usage] Query Account Statements';

  constructor(public pageable?: PageableModel, public startDate?: Date, public endDate?: Date) {}
}

export class ResetSmsUsage {
  static readonly type = '[Usage] Reset SMS Usage';
}

export class GetSmsUsage {
  static readonly type = '[Usage] Get SMS Usage';

  constructor(public startDate: Date, public endDate: Date) {}
}
