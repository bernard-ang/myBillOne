import {
  SmsActivityCreateRequestModel,
  SmsActivityRequestModel,
  SmsTypeRequestModel,
  PageableModel,
  BaseIndexRowModel,
} from '@grabbill/lib';

export class ResetSmsTypes {
  static readonly type = '[Sms] ResetSmsTypes';
}

export class QuerySmsTypes {
  static readonly type = '[Sms] QuerySmsTypes';

  constructor(public pageable?: PageableModel, public name?: string) {}
}

export class LoadMoreSmsTypes {
  static readonly type = '[Sms] LoadMoreSmsTypes';
}

export class GetSmsType {
  static readonly type = '[Sms] GetSmsType';

  constructor(public typeId: number) {}
}

export class ResetSmsType {
  static readonly type = '[Sms] ResetSmsType';
}

export class NewSmsType {
  static readonly type = '[Sms] NewSmsType';

  constructor(public request: SmsTypeRequestModel) {}
}

export class UpdateSmsType {
  static readonly type = '[Sms] UpdateSmsType';

  constructor(public typeId: number, public request: SmsTypeRequestModel) {}
}

export class DeleteSmsType {
  static readonly type = '[Sms] DeleteSmsType';

  constructor(public typeId: number) {}
}

export class DuplicateSmsType {
  static readonly type = '[Sms] DuplicateSmsType';

  constructor(public typeId: number) {}
}

export class QuerySmsActivities {
  static readonly type = '[Sms] QuerySmsActivities';

  constructor(public typeId: number, public pageable?: PageableModel, public name?: string) {}
}

export class LoadMoreSmsActivities {
  static readonly type = '[Sms] LoadMoreSmsActivities';

  constructor(public typeId: number) {}
}

export class GetSmsActivity {
  static readonly type = '[Sms] GetSmsActivity';

  constructor(public typeId: number, public activityId: number) {}
}

export class ResetSmsActivity {
  static readonly type = '[Sms] ResetSmsActivity';
}

export class NewSmsActivity {
  static readonly type = '[Sms] NewSmsActivity';

  constructor(public typeId: number, public request: SmsActivityCreateRequestModel) {}
}

export class UpdateSmsActivity {
  static readonly type = '[Sms] UpdateSmsActivity';

  constructor(public typeId: number, public activityId: number, public request: SmsActivityRequestModel) {}
}

export class DeleteSmsActivity {
  static readonly type = '[Sms] DeleteSmsActivity';

  constructor(public typeId: number, public activityId: number) {}
}

export class CountSmsCreditUsage {
  static readonly type = '[Sms] CountSmsCreditUsage';

  constructor(
    public typeId: number,
    public activityId: number,
    public smsContent: string,
    public contactGroupId?: string,
    public indexRows?: BaseIndexRowModel[]
  ) {}
}
