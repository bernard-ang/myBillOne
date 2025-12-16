import { PageableModel } from '@grabbill/lib';

export class ResetJobs {
  static readonly type = '[Job] ResetJobs';
}

export class QueryJobs {
  static readonly type = '[Job] QueryJobs';

  constructor(
    public pageable: PageableModel,
    public filters: { [key: string]: any },
    public startDate?: Date,
    public endDate?: Date
  ) {}
}

export class ResetJob {
  static readonly type = '[Job] ResetJob';
}

export class GetJob {
  static readonly type = '[Job] GetJob';

  constructor(public id: number) {}
}

export class RetryJob {
  static readonly type = '[Job] RetryJob';

  constructor(public id: number) {}
}
