import { PageableModel } from '@grabbill/lib';

export class ResetJobs {
  static readonly type = '[Job Management] ResetJobs';
}

export class QueryJobs {
  static readonly type = '[Job Management] QueryJobs';

  constructor(
    public pageable: PageableModel,
    public filters: { [key: string]: any },
    public accountName: string,
    public startDate?: Date,
    public endDate?: Date
  ) {}
}

export class ResetJob {
  static readonly type = '[Job Management] ResetJob';
}

export class GetJob {
  static readonly type = '[Job Management] GetJob';

  constructor(public id: number) {}
}

export class RetryJob {
  static readonly type = '[Job Management] RetryJob';

  constructor(public id: number) {}
}
