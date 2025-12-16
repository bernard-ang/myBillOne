import { NzMessageType } from 'ng-zorro-antd/message';
import { Breadcrumb } from '@grabbill/lib';

export class ShowMessage {
  static readonly type = '[Admin Common] ShowMessage';

  constructor(public messageType: NzMessageType, public message: string) {}
}

export class ResetMessage {
  static readonly type = '[Admin Common] ResetMessage';
}

export class UpdateBreadcrumb {
  static readonly type = '[Admin Common] UpdateBreadcrumb';

  constructor(public breadcrumbs: Breadcrumb[]) {}
}

export class UpdateSectionTitle {
  static readonly type = '[Admin Common] UpdateSectionTitle';

  constructor(public sectionTitle: string) {}
}

export class UpdateError {
  static readonly type = '[Admin Common] UpdateError';

  constructor(public error?: string) {}
}

export class SetPageLoading {
  static readonly type = '[Common] SetPageLoading';

  constructor(public loading: boolean) {}
}
