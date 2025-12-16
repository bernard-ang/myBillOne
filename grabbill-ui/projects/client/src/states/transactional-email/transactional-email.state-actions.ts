import {
  BaseIndexRowModel,
  PageableModel,
  TransactionalEmailActivityCreateRequestModel,
  TransactionalEmailActivityRequestModel,
  TransactionalEmailTypeModel,
  TransactionalEmailTypeRequestModel,
} from '@grabbill/lib';

export class ResetTransactionalEmailTypes {
  static readonly type = '[TransactionalEmail] ResetTransactionalEmailTypes';
}

export class QueryTransactionalEmailTypes {
  static readonly type = '[TransactionalEmail] QueryTransactionalEmailTypes';

  constructor(public pageable?: PageableModel, public name?: string) {}
}

export class LoadMoreTransactionalEmailTypes {
  static readonly type = '[TransactionalEmail] LoadMoreTransactionalEmailTypes';
}

export class GetTransactionalEmailType {
  static readonly type = '[TransactionalEmail] GetTransactionalEmailType';

  constructor(public typeId: number) {}
}

export class ResetTransactionalEmailType {
  static readonly type = '[TransactionalEmail] ResetTransactionalEmailType';
}

export class NewTransactionalEmailType {
  static readonly type = '[TransactionalEmail] NewTransactionalEmailType';

  constructor(public request: TransactionalEmailTypeRequestModel) {}
}

export class UpdateTransactionalEmailType {
  static readonly type = '[TransactionalEmail] UpdateTransactionalEmailType';

  constructor(public typeId: number, public request: TransactionalEmailTypeRequestModel) {}
}

export class DeleteTransactionalEmailType {
  static readonly type = '[TransactionalEmail] DeleteTransactionalEmailType';

  constructor(public typeId: number) {}
}

export class DuplicateTransactionalEmailType {
  static readonly type = '[TransactionalEmail] DuplicateTransactionalEmailType';

  constructor(public typeId: number) {}
}

export class QueryTransactionalEmailActivities {
  static readonly type = '[TransactionalEmail] QueryTransactionalEmailActivities';

  constructor(public typeId: number, public pageable?: PageableModel, public name?: string) {}
}

export class LoadMoreTransactionalEmailActivities {
  static readonly type = '[TransactionalEmail] LoadMoreTransactionalEmailActivities';

  constructor(public typeId: number) {}
}

export class GetTransactionalEmailActivity {
  static readonly type = '[TransactionalEmail] GetTransactionalEmailActivity';

  constructor(public typeId: number, public activityId: number) {}
}

export class ResetTransactionalEmailActivity {
  static readonly type = '[TransactionalEmail] ResetTransactionalEmailActivity';
}

export class NewTransactionalEmailActivity {
  static readonly type = '[TransactionalEmail] NewTransactionalEmailActivity';

  constructor(public typeId: number, public request: TransactionalEmailActivityCreateRequestModel) {}
}

export class UpdateTransactionalEmailActivity {
  static readonly type = '[TransactionalEmail] UpdateTransactionalEmailActivity';

  constructor(
    public typeId: number,
    public activityId: number,
    public request: TransactionalEmailActivityRequestModel
  ) {}
}

export class DeleteTransactionalEmailActivity {
  static readonly type = '[TransactionalEmail] DeleteTransactionalEmailActivity';

  constructor(public typeId: number, public activityId: number) {}
}

export class ExportTransactionalEmailActivity {
  static readonly type = '[TransactionalEmail] ExportTransactionalEmailActivity';

  constructor(public typeId: number, public activityId: number) {}
}

export class PurgeTransactionalEmailActivity {
  static readonly type = '[TransactionalEmail] PurgeTransactionalEmailActivity';

  constructor(public typeId: number, public activityId: number) {}
}

export class UploadTransactionalEmailFile {
  static readonly type = '[TransactionalEmail] UploadTransactionalEmailFile';

  constructor(public typeId: number, public activityId: number, public fileFormData: FormData) {}
}

export class DeleteTransactionalEmailFiles {
  static readonly type = '[TransactionalEmail] DeleteTransactionalEmailFiles';

  constructor(public typeId: number, public activityId: number) {}
}

export class DeleteTransactionalEmailFile {
  static readonly type = '[TransactionalEmail] DeleteTransactionalEmailFile';

  constructor(public typeId: number, public activityId: number, public fileId: number) {}
}

export class DownloadTransactionalEmailFile {
  static readonly type = '[TransactionalEmail] DownloadTransactionalEmailFile';

  constructor(public typeId: number, public activityId: number, public fileId: number, public filename: string) {}
}

export class DownloadIndexData {
  static readonly type = '[TransactionalEmail] DownloadIndexData';

  constructor(
    public indexRows: BaseIndexRowModel[],
    public type: TransactionalEmailTypeModel,
    public generateSampleData: boolean = false
  ) {}
}

export class QueryTransactionalEmailTypeFiles {
  static readonly type = '[TransactionalEmail] QueryTransactionalEmailTypeFiles';

  constructor(
    public typeId: number,
    public pageable?: PageableModel,
    public name?: string,
    public fileFilters?: { [index: string]: any }
  ) {}
}
