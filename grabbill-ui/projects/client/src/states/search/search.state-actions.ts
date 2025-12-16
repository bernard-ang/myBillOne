import { DomainType, PageableModel } from '@grabbill/lib';

export class ResetSearch {
  static readonly type = '[Search] ResetSearch';
}

export class QueryTypes {
  static readonly type = '[Search] QueryTypes';

  constructor(public domain: DomainType, public pageable: PageableModel, public typeName?: string) {}
}

export class GetType {
  static readonly type = '[Search] GetType';

  constructor(public domain: DomainType, public typeId: number) {}
}

export class DeleteFile {
  static readonly type = '[Search] DeleteFile';

  constructor(public domain: DomainType, public typeId: number, public activityId: number, public fileId: number) {}
}

export class DownloadFile {
  static readonly type = '[Search] DownloadFile';

  constructor(
    public domain: DomainType,
    public typeId: number,
    public activityId: number,
    public fileId: number,
    public filename: string
  ) {}
}

export class QueryTypeIndexRecords {
  static readonly type = '[Search] QueryTypeIndexRecords';

  constructor(
    public domain: DomainType,
    public typeId: number,
    public pageable?: PageableModel,
    public indexRowFilters?: { [index: string]: any }
  ) {}
}

export class ResetIndexRecords {
  static readonly type = '[Search] ResetIndexRecords';
}
