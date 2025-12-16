import {
  BaseIndexRowModel,
  PageableModel,
  MultiTemplateTransactionalEmailActivityRequestModel,
  MultiTemplateTransactionalEmailTypeModel,
  MultiTemplateTransactionalEmailTypeRequestModel,
  TransactionalEmailActivityCreateRequestModel,
  MultiTemplateTransactionalEmailActivityCreateRequestModel, DigitalFilingActivityRequestModel
} from "@grabbill/lib";

export class ResetMultiTemplateTransactionalEmailTypes {
  static readonly type = '[MultiTemplateTransactionalEmail] ResetMultiTemplateTransactionalEmailTypes';
}

export class QueryMultiTemplateTransactionalEmailTypes {
  static readonly type = '[MultiTemplateTransactionalEmail] QueryMultiTemplateTransactionalEmailTypes';

  constructor(public pageable?: PageableModel, public name?: string) {}
}

export class LoadMoreMultiTemplateTransactionalEmailTypes {
  static readonly type = '[MultiTemplateTransactionalEmail] LoadMoreMultiTemplateTransactionalEmailTypes';
}

export class GetMultiTemplateTransactionalEmailType {
  static readonly type = '[MultiTemplateTransactionalEmail] GetMultiTemplateTransactionalEmailType';

  constructor(public typeId: number) {}
}

export class ResetMultiTemplateTransactionalEmailType {
  static readonly type = '[MultiTemplateTransactionalEmail] ResetMultiTemplateTransactionalEmailType';
}

export class NewMultiTemplateTransactionalEmailType {
  static readonly type = '[MultiTemplateTransactionalEmail] NewMultiTemplateTransactionalEmailType';

  constructor(public request: MultiTemplateTransactionalEmailTypeRequestModel) {}
}

export class UpdateMultiTemplateTransactionalEmailType {
  static readonly type = '[MultiTemplateTransactionalEmail] UpdateMultiTemplateTransactionalEmailType';

  constructor(public typeId: number, public request: MultiTemplateTransactionalEmailTypeRequestModel) {}
}

export class DeleteMultiTemplateTransactionalEmailType {
  static readonly type = '[MultiTemplateTransactionalEmail] DeleteMultiTemplateTransactionalEmailType';

  constructor(public typeId: number) {}
}

export class DuplicateMultiTemplateTransactionalEmailType {
  static readonly type = '[MultiTemplateTransactionalEmail] DuplicateMultiTemplateTransactionalEmailType';

  constructor(public typeId: number) {}
}

export class QueryMultiTemplateTransactionalEmailActivities {
  static readonly type = '[MultiTemplateTransactionalEmail] QueryMultiTemplateTransactionalEmailActivities';

  constructor(public typeId: number, public pageable?: PageableModel, public name?: string) {}
}

export class LoadMoreMultiTemplateTransactionalEmailActivities {
  static readonly type = '[MultiTemplateTransactionalEmail] LoadMoreMultiTemplateTransactionalEmailActivities';

  constructor(public typeId: number) {}
}

export class GetMultiTemplateTransactionalEmailActivity {
  static readonly type = '[MultiTemplateTransactionalEmail] GetMultiTemplateTransactionalEmailActivity';

  constructor(public typeId: number, public activityId: number) {}
}

export class ResetMultiTemplateTransactionalEmailActivity {
  static readonly type = '[MultiTemplateTransactionalEmail] ResetMultiTemplateTransactionalEmailActivity';
}

export class NewMultiTemplateTransactionalEmailActivity {
  static readonly type = '[MultiTemplateTransactionalEmail] NewMultiTemplateTransactionalEmailActivity';

  constructor(public typeId: number, public request: MultiTemplateTransactionalEmailActivityCreateRequestModel) {}
}

export class UpdateMultiTemplateTransactionalEmailActivity {
  static readonly type = '[MultiTemplateTransactionalEmail] UpdateMultiTemplateTransactionalEmailActivity';

  constructor(
    public typeId: number,
    public activityId: number,
    public request: MultiTemplateTransactionalEmailActivityRequestModel
  ) {}
}

export class DeleteMultiTemplateTransactionalEmailActivity {
  static readonly type = '[MultiTemplateTransactionalEmail] DeleteMultiTemplateTransactionalEmailActivity';

  constructor(public typeId: number, public activityId: number) {}
}

export class ExportMultiTemplateTransactionalEmailActivity {
  static readonly type = '[MultiTemplateTransactionalEmail] ExportMultiTemplateTransactionalEmailActivity';

  constructor(public typeId: number, public activityId: number) {}
}

export class PurgeMultiTemplateTransactionalEmailActivity {
  static readonly type = '[MultiTemplateTransactionalEmail] PurgeMultiTemplateTransactionalEmailActivity';

  constructor(public typeId: number, public activityId: number) {}
}

export class UploadMultiTemplateTransactionalEmailFile {
  static readonly type = '[MultiTemplateTransactionalEmail] UploadMultiTemplateTransactionalEmailFile';

  constructor(public typeId: number, public activityId: number, public fileFormData: FormData) {}
}

export class DeleteMultiTemplateTransactionalEmailFiles {
  static readonly type = '[MultiTemplateTransactionalEmail] DeleteMultiTemplateTransactionalEmailFiles';

  constructor(public typeId: number, public activityId: number) {}
}

export class DeleteMultiTemplateTransactionalEmailFile {
  static readonly type = '[MultiTemplateTransactionalEmail] DeleteMultiTemplateTransactionalEmailFile';

  constructor(public typeId: number, public activityId: number, public fileId: number) {}
}

export class DownloadMultiTemplateTransactionalEmailFile {
  static readonly type = '[MultiTemplateTransactionalEmail] DownloadMultiTemplateTransactionalEmailFile';

  constructor(public typeId: number, public activityId: number, public fileId: number, public filename: string) {}
}

export class DownloadMultiTemplateIndexData {
  static readonly type = '[MultiTemplateTransactionalEmail] DownloadIndexData';

  constructor(
    public indexRows: BaseIndexRowModel[],
    public type: MultiTemplateTransactionalEmailTypeModel,
    public generateSampleData: boolean = false
  ) {}
}

export class QueryMultiTemplateTransactionalEmailTypeFiles {
  static readonly type = '[MultiTemplateTransactionalEmail] QueryMultiTemplateTransactionalEmailTypeFiles';

  constructor(
    public typeId: number,
    public pageable?: PageableModel,
    public name?: string,
    public fileFilters?: { [index: string]: any }
  ) {}
}

export class ValidateMultiTemplateTransactionalEmailActivitySftp {
  static readonly type = '[MultiTemplateTransactionalEmail] ValidateMultiTemplateTransactionalEmailActivitySftp';

  constructor(public typeId: number, public activityId: number, public request: MultiTemplateTransactionalEmailActivityRequestModel) {}
}
