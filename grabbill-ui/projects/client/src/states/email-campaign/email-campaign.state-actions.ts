import {
  EmailCampaignActivityCreateRequestModel,
  EmailCampaignActivityRequestModel,
  EmailCampaignTypeRequestModel,
  PageableModel,
} from '@grabbill/lib';

export class ResetEmailCampaignTypes {
  static readonly type = '[EmailCampaign] ResetEmailCampaignTypes';
}

export class QueryEmailCampaignTypes {
  static readonly type = '[EmailCampaign] QueryEmailCampaignTypes';

  constructor(public pageable?: PageableModel, public name?: string) {}
}

export class LoadMoreEmailCampaignTypes {
  static readonly type = '[EmailCampaign] LoadMoreEmailCampaignTypes';
}

export class GetEmailCampaignType {
  static readonly type = '[EmailCampaign] GetEmailCampaignType';

  constructor(public typeId: number) {}
}

export class ResetEmailCampaignType {
  static readonly type = '[EmailCampaign] ResetEmailCampaignType';
}

export class NewEmailCampaignType {
  static readonly type = '[EmailCampaign] NewEmailCampaignType';

  constructor(public request: EmailCampaignTypeRequestModel) {}
}

export class UpdateEmailCampaignType {
  static readonly type = '[EmailCampaign] UpdateEmailCampaignType';

  constructor(public typeId: number, public request: EmailCampaignTypeRequestModel) {}
}

export class DeleteEmailCampaignType {
  static readonly type = '[EmailCampaign] DeleteEmailCampaignType';

  constructor(public typeId: number) {}
}

export class DuplicateEmailCampaignType {
  static readonly type = '[EmailCampaign] DuplicateEmailCampaignType';

  constructor(public typeId: number) {}
}

export class QueryEmailCampaignActivities {
  static readonly type = '[EmailCampaign] QueryEmailCampaignActivities';

  constructor(public typeId: number, public pageable?: PageableModel, public name?: string) {}
}

export class LoadMoreEmailCampaignActivities {
  static readonly type = '[EmailCampaign] LoadMoreEmailCampaignActivities';

  constructor(public typeId: number) {}
}

export class GetEmailCampaignActivity {
  static readonly type = '[EmailCampaign] GetEmailCampaignActivity';

  constructor(public typeId: number, public activityId: number) {}
}

export class ResetEmailCampaignActivity {
  static readonly type = '[EmailCampaign] ResetEmailCampaignActivity';
}

export class NewEmailCampaignActivity {
  static readonly type = '[EmailCampaign] NewEmailCampaignActivity';

  constructor(public typeId: number, public request: EmailCampaignActivityCreateRequestModel) {}
}

export class UpdateEmailCampaignActivity {
  static readonly type = '[EmailCampaign] UpdateEmailCampaignActivity';

  constructor(public typeId: number, public activityId: number, public request: EmailCampaignActivityRequestModel) {}
}

export class DeleteEmailCampaignActivity {
  static readonly type = '[EmailCampaign] DeleteEmailCampaignActivity';

  constructor(public typeId: number, public activityId: number) {}
}

export class ExportEmailCampaignActivity {
  static readonly type = '[EmailCampaign] ExportEmailCampaignActivity';

  constructor(public typeId: number, public activityId: number) {}
}

export class PurgeEmailCampaignActivity {
  static readonly type = '[EmailCampaign] PurgeEmailCampaignActivity';

  constructor(public typeId: number, public activityId: number) {}
}

export class UploadEmailCampaignFile {
  static readonly type = '[EmailCampaign] UploadEmailCampaignFile';

  constructor(public typeId: number, public activityId: number, public fileFormData: FormData) {}
}

export class DeleteEmailCampaignFiles {
  static readonly type = '[EmailCampaign] DeleteEmailCampaignFiles';

  constructor(public typeId: number, public activityId: number) {}
}

export class DeleteEmailCampaignFile {
  static readonly type = '[EmailCampaign] DeleteEmailCampaignFile';

  constructor(public typeId: number, public activityId: number, public fileId: number) {}
}

export class DownloadEmailCampaignFile {
  static readonly type = '[EmailCampaign] DownloadEmailCampaignFile';

  constructor(public typeId: number, public activityId: number, public fileId: number, public filename: string) {}
}

export class QueryEmailCampaignTypeFiles {
  static readonly type = '[EmailCampaign] QueryEmailCampaignTypeFiles';

  constructor(
    public typeId: number,
    public pageable?: PageableModel,
    public name?: string,
    public fileFilters?: { [index: string]: any }
  ) {}
}
