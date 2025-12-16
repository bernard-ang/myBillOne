import {
  BaseIndexRowModel,
  DigitalFilingActivityCreateRequestModel,
  DigitalFilingActivityRequestModel,
  DigitalFilingTypeModel,
  DigitalFilingTypeRequestModel,
  PageableModel,
} from "@grabbill/lib";

export class QueryDigitalFilingTypes {
  static readonly type = '[DigitalFiling] QueryDigitalFilingTypes';

  constructor(public pageable?: PageableModel, public name?: string) {}
}

export class ResetDigitalFilingTypes {
  static readonly type = '[DigitalFiling] ResetDigitalFilingTypes';
}

export class LoadMoreDigitalFilingTypes {
  static readonly type = '[DigitalFiling] LoadMoreDigitalFilingTypes';
}

export class GetDigitalFilingType {
  static readonly type = '[DigitalFiling] GetDigitalFilingType';

  constructor(public typeId: number) {}
}

export class ResetDigitalFilingType {
  static readonly type = '[DigitalFiling] ResetDigitalFilingType';
}

export class NewDigitalFilingType {
  static readonly type = '[DigitalFiling] NewDigitalFilingType';

  constructor(public request: DigitalFilingTypeRequestModel) {}
}

export class UpdateDigitalFilingType {
  static readonly type = '[DigitalFiling] UpdateDigitalFilingType';

  constructor(public typeId: number, public request: DigitalFilingTypeRequestModel) {}
}

export class DeleteDigitalFilingType {
  static readonly type = '[DigitalFiling] DeleteDigitalFilingType';

  constructor(public typeId: number) {}
}

export class DuplicateDigitalFilingType {
  static readonly type = '[DigitalFiling] DuplicateDigitalFilingType';

  constructor(public typeId: number) {}
}

export class QueryDigitalFilingActivities {
  static readonly type = '[DigitalFiling] QueryDigitalFilingActivities';

  constructor(public typeId: number, public pageable?: PageableModel, public name?: string) {}
}

export class LoadMoreDigitalFilingActivities {
  static readonly type = '[DigitalFiling] LoadMoreDigitalFilingActivities';

  constructor(public typeId: number) {}
}

export class GetDigitalFilingActivity {
  static readonly type = '[DigitalFiling] GetDigitalFilingActivity';

  constructor(public typeId: number, public activityId: number) {}
}

export class ResetDigitalFilingActivity {
  static readonly type = '[DigitalFiling] ResetDigitalFilingActivity';
}

export class NewDigitalFilingActivity {
  static readonly type = '[DigitalFiling] NewDigitalFilingActivity';

  constructor(public typeId: number, public request: DigitalFilingActivityCreateRequestModel) {}
}

export class UpdateDigitalFilingActivity {
  static readonly type = '[DigitalFiling] UpdateDigitalFilingActivity';

  constructor(public typeId: number, public activityId: number, public request: DigitalFilingActivityRequestModel) {}
}

export class DeleteDigitalFilingActivity {
  static readonly type = '[DigitalFiling] DeleteDigitalFilingActivity';

  constructor(public typeId: number, public activityId: number) {}
}

export class ExportDigitalFilingActivity {
  static readonly type = '[DigitalFiling] ExportDigitalFilingActivity';

  constructor(public typeId: number, public activityId: number) {}
}

export class PurgeDigitalFilingActivity {
  static readonly type = '[DigitalFiling] PurgeDigitalFilingActivity';

  constructor(public typeId: number, public activityId: number) {}
}

export class UploadDigitalFilingFile {
  static readonly type = '[DigitalFiling] UploadDigitalFilingFile';

  constructor(public typeId: number, public activityId: number, public fileFormData: FormData) {}
}

export class DeleteDigitalFilingFiles {
  static readonly type = '[DigitalFiling] DeleteDigitalFilingFiles';

  constructor(public typeId: number, public activityId: number) {}
}

export class DeleteDigitalFilingFile {
  static readonly type = '[DigitalFiling] DeleteDigitalFilingFile';

  constructor(public typeId: number, public activityId: number, public fileId: number) {}
}

export class DownloadDigitalFilingFile {
  static readonly type = '[DigitalFiling] DownloadDigitalFilingFile';

  constructor(public typeId: number, public activityId: number, public fileId: number, public filename: string) {}
}

export class DownloadDigitalFilingFiles {
  static readonly type = '[DigitalFiling] DownloadDigitalFilingFiles';

  constructor(public typeId: number) {}
}

export class DownloadIndexData {
  static readonly type = '[DigitalFiling] DownloadIndexData';

  constructor(public indexRows: BaseIndexRowModel[], public type: DigitalFilingTypeModel, public generateSampleData: boolean = false) {}
}

export class QueryDigitalFilingTypeFiles {
  static readonly type = '[DigitalFiling] QueryDigitalFilingTypeFiles';

  constructor(
    public typeId: number,
    public pageable?: PageableModel,
    public name?: string,
    public fileFilters?: { [index: string]: any }
  ) {}
}

export class ValidateDigitalFilingActivitySftp {
  static readonly type = '[DigitalFiling] ValidateDigitalFilingActivitySftp';

  constructor(public typeId: number, public activityId: number, public request: DigitalFilingActivityRequestModel) {}
}
