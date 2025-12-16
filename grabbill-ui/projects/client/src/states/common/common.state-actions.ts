import { Breadcrumb } from '@grabbill/lib';

export class ShowMessage {
  static readonly type = '[Common] ShowMessage';

  constructor(public messageType: string, public message: string) {}
}

export class ResetMessage {
  static readonly type = '[Common] ResetMessage';
}

export class UpdateBreadcrumb {
  static readonly type = '[Common] UpdateBreadcrumb';

  constructor(public breadcrumbs: Breadcrumb[]) {}
}

export class UpdateSectionTitle {
  static readonly type = '[Common] UpdateSectionTitle';

  constructor(public sectionTitle: string) {}
}

export class UpdateError {
  static readonly type = '[Common] UpdateError';

  constructor(public error?: string) {}
}

export class SetPageLoading {
  static readonly type = '[Common] SetPageLoading';

  constructor(public loading: boolean) {}
}

export class OpenContextSensitiveHelp {
  static readonly type = '[Common] OpenContextSensitiveHelp';

  constructor(public route: string) {}
}

export class CloseContextSensitiveHelp {
  static readonly type = '[Common] CloseContextSensitiveHelp';
}
