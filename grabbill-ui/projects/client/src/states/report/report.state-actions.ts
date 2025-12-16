import { DomainType, PageableModel } from '@grabbill/lib';

export class ResetReport {
  static readonly type = '[Report] ResetReport';
}

export class QueryTypes {
  static readonly type = '[Report] QueryTypes';

  constructor(public domain: DomainType, public pageable: PageableModel, public typeName?: string) {}
}

export class QueryActivities {
  static readonly type = '[Report] QueryActivities';

  constructor(
    public domain: DomainType,
    public pageable: PageableModel,
    public typeId: number,
    public activityName?: string
  ) {}
}
export class GenerateReport {
  static readonly type = '[Report] GenerateReport';

  constructor(
    public domain: DomainType,
    public typeId: number,
    public activityIds: number[],
    public reportTypes: string[],
    public encrypts: string[]
  ) {}
}
